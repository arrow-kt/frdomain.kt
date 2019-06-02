package io.arrowkt.tagless.service.interpreter

import arrow.Kind
import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.getOrElse
import arrow.data.Kleisli
import arrow.data.ReaderT
import arrow.data.extensions.kleisli.monad.monad
import arrow.data.fix
import arrow.typeclasses.MonadError
import io.arrowkt.tagless.Amount
import io.arrowkt.tagless.ErrorOr
import io.arrowkt.tagless.model.Account
import io.arrowkt.tagless.model.Balance
import io.arrowkt.tagless.repository.AccountRepository
import io.arrowkt.tagless.service.AccountService
import io.arrowkt.tagless.service.AccountType
import io.arrowkt.tagless.today
import java.math.BigDecimal
import java.time.LocalDate

@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
class AccountServiceInterpreter<F>(
    me: MonadError<F, Throwable>
) : AccountService<F, Account, Amount, Balance>, MonadError<F, Throwable> by me {

    override fun open(
        no: String,
        name: String,
        rate: Option<BigDecimal>,
        openingDate: Option<LocalDate>,
        accountType: AccountType
    ): Kleisli<F, AccountRepository<F>, Account> = Kleisli { repo ->
        repo.query(no)
            .flatMap { maybeAccount ->
                doOpenAccount(
                    repo,
                    maybeAccount,
                    no,
                    name,
                    rate,
                    openingDate,
                    accountType
                )
            }
    }

    private fun doOpenAccount(
        repo: AccountRepository<F>,
        maybeAccount: Option<Account>,
        no: String,
        name: String,
        rate: Option<BigDecimal>,
        openingDate: Option<LocalDate>,
        accountType: AccountType
    ): Kind<F, Account> =
        maybeAccount.map { raiseError<Account>(IllegalArgumentException("Account no $no already exists")) }
            .getOrElse { createOrUpdate(repo, no, name, rate, openingDate, accountType) }

    private fun createOrUpdate(
        repo: AccountRepository<F>,
        no: String,
        name: String,
        rate: Option<BigDecimal>,
        openingDate: Option<LocalDate>,
        accountType: AccountType
    ): Kind<F, Account> = when (accountType) {
        AccountType.CHECKING -> createOrUpdate(repo, Account.checkingAccount(no, name, openingDate, None, Balance()))
        AccountType.SAVINGS -> rate.map {
            createOrUpdate(
                repo,
                Account.savingsAccount(no, name, it, openingDate, None, Balance())
            )
        }
            .getOrElse { raiseError(IllegalArgumentException("Rate missing for savings account")) }
    }

    private fun createOrUpdate(
        repo: AccountRepository<F>,
        errorOrAccount: ErrorOr<Account>
    ): Kind<F, Account> = when (errorOrAccount) {
        is Either.Left -> raiseError(IllegalArgumentException("${errorOrAccount.a}"))
        is Either.Right -> repo.store(errorOrAccount.b)
    }

    override fun close(no: String, closeDate: Option<LocalDate>): Kleisli<F, AccountRepository<F>, Account> =
        Kleisli { repo ->
            binding {
                val maybeAccount = repo.query(no).bind()
                val account =
                    maybeAccount.map { a -> createOrUpdate(repo, Account.close(a, closeDate.getOrElse { today() })) }
                        .getOrElse { raiseError(IllegalArgumentException("Account no $no does not exist")) }.bind()
                account
            }
        }

    private sealed class DC {
        object D : DC()
        object C : DC()
    }

    override fun debit(no: String, amount: Amount) = update(no, amount, DC.D)

    override fun credit(no: String, amount: Amount) = update(no, amount, DC.C)

    private fun update(no: String, amount: Amount, debitCredit: DC): Kleisli<F, AccountRepository<F>, Account> =
        Kleisli { repo ->
            binding {
                val maybeAccount = repo.query(no).bind()
                val multiplier = if (debitCredit == DC.D) -1L else 1L
                val account = maybeAccount.map { a ->
                    createOrUpdate(
                        repo, Account.updateBalance(
                            a,
                            amount.multiply(BigDecimal.valueOf(multiplier))
                        )
                    )
                }.getOrElse { raiseError(IllegalArgumentException("Account no $no does not exist")) }.bind()
                account
            }
        }

    override fun balance(no: String): Kleisli<F, AccountRepository<F>, Balance> =
        Kleisli { repo -> repo.balance(no) }

    override fun transfer(
        from: String,
        to: String,
        amount: Amount
    ): Kleisli<F, AccountRepository<F>, Pair<Account, Account>> =
        ReaderT.monad<F, AccountRepository<F>>(this).binding {
            val a = debit(from, amount).bind()
            val b = credit(to, amount).bind()
            Pair(a, b)
        }.fix()

}