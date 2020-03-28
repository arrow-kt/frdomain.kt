package io.arrowkt.arrowio.service.interpreter

import arrow.core.None
import arrow.core.Option
import arrow.core.getOrElse
import arrow.fx.IO
import arrow.fx.extensions.toIO
import arrow.fx.flatMap
import arrow.fx.mapError
import arrow.mtl.Kleisli
import io.arrowkt.arrowio.Amount
import io.arrowkt.arrowio.DomainError
import io.arrowkt.arrowio.model.Account
import io.arrowkt.arrowio.model.Balance
import io.arrowkt.arrowio.repository.AccountRepository
import io.arrowkt.arrowio.service.*
import io.arrowkt.arrowio.service.AccountService
import io.arrowkt.arrowio.today
import java.math.BigDecimal
import java.time.LocalDate

object AccountService : AccountService<Account, Amount, Balance> {

    override fun open(
        no: String,
        name: String,
        rate: Option<BigDecimal>,
        openingDate: Option<LocalDate>,
        accountType: AccountType
    ): AccountOperation<Account> = Kleisli.invoke { repo ->
        repo.query(no).flatMap { accountOpt ->
            accountOpt.fold(
                ifEmpty = {
                    when (accountType) {
                        AccountType.CHECKING -> createOrUpdate(
                            repo,
                            Account.checkingAccount(no, name, openingDate, None, Balance()).toIO()
                        )
                        AccountType.SAVINGS -> rate.map { r ->
                            createOrUpdate(repo, Account.savingsAccount(no, name, r, openingDate, None, Balance()).toIO())
                        }.getOrElse { IO.raiseError(RateMissingForSavingsAccount) }
                    }
                },
                ifSome = { IO.raiseError<AlreadyExistingAccount, Account>(AlreadyExistingAccount(no)) }
            )
        }
    }

    private fun createOrUpdate(
        repo: AccountRepository,
        errorOrAccount: IO<DomainError, Account>
    ): IO<AccountServiceException, Account> =
        errorOrAccount.flatMap { repo.store(it) }
            .mapError { MiscellaneousDomainExceptions(it) }

    override fun close(no: String, closeDate: Option<LocalDate>): AccountOperation<Account> =
        Kleisli.invoke { repo ->
            repo.query(no).flatMap { accountOpt ->
                accountOpt.fold(
                    ifEmpty = { IO.raiseError<NonExistingAccount, Account>(NonExistingAccount(no)) },
                    ifSome = {
                        val cd = closeDate.getOrElse { today() }
                        createOrUpdate(repo, Account.close(it, cd).toIO())
                    }
                )
            }
        }

    private sealed class DC {
        object D : DC()
        object C : DC()
    }

    override fun debit(no: String, amount: Amount): AccountOperation<Account> =
        up(no, amount, DC.D)

    override fun credit(no: String, amount: Amount): AccountOperation<Account> =
        up(no, amount, DC.C)

    private fun up(no: String, amount: Amount, dc: DC): AccountOperation<Account> =
        Kleisli { repo ->
            repo.query(no).flatMap { accountOpt ->
                accountOpt.fold(
                    ifEmpty = { IO.raiseError<NonExistingAccount, Account>(NonExistingAccount(no)) },
                    ifSome = {
                        val updated = when (dc) {
                            is DC.D -> createOrUpdate(repo, Account.updateBalance(it, -amount).toIO())
                            is DC.C -> createOrUpdate(repo, Account.updateBalance(it, amount).toIO())
                        }
                        updated
                    }
                )
            }
        }

    override fun balance(no: String): AccountOperation<Balance> =
        Kleisli { repo ->
            repo.balance(no).map { it }
                .mapError { MiscellaneousDomainExceptions(it) }
        }
}