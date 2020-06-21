package io.arrowkt.frdomain.service.interpreter

import arrow.core.*
import io.arrowkt.frdomain.Amount
import io.arrowkt.frdomain.ErrorOr
import io.arrowkt.frdomain.model.Account
import io.arrowkt.frdomain.model.Balance
import io.arrowkt.frdomain.repository.AccountRepository
import io.arrowkt.frdomain.service.*
import io.arrowkt.frdomain.service.AccountService
import io.arrowkt.frdomain.today
import java.math.BigDecimal
import java.time.LocalDate

object AccountService : AccountService<AccountRepository, Account, Amount, Balance> {

    override suspend fun AccountRepository.open(
        no: String,
        name: String,
        rate: BigDecimal?,
        openingDate: LocalDate?,
        accountType: AccountType
    ): Either<AccountServiceException, Account> = either {
        val accountOrNull = query(no).mapLeft { MiscellaneousDomainExceptions(it) }.bind()
        accountOrNull.rightIfNull { AlreadyExistingAccount(no) }.bind()

        val accountOrError = when (accountType) {
            AccountType.CHECKING -> Account.checkingAccount(no, name, openingDate, null, Balance())
            AccountType.SAVINGS -> {
                val r = rate.rightIfNotNull { RateMissingForSavingsAccount }.bind()
                Account.savingsAccount(no, name, r, openingDate, null, Balance())
            }
        }
        createOrUpdate(this@open, accountOrError).bind()
    }

    private suspend fun createOrUpdate(
        repo: AccountRepository, errorOrAccount: ErrorOr<Account>
    ): Either<AccountServiceException, Account> =
        when (errorOrAccount) {
            is Either.Left -> MiscellaneousDomainExceptions(errorOrAccount.a).left()
            is Either.Right -> repo.store(errorOrAccount.b)
                .mapLeft { MiscellaneousDomainExceptions(it) }
        }

    override suspend fun AccountRepository.close(
        no: String,
        closeDate: LocalDate?
    ): Either<AccountServiceException, Account> = either {
        val accountOrNull = query(no).mapLeft { MiscellaneousDomainExceptions(it) }.bind()
        val account = accountOrNull.rightIfNotNull { NonExistingAccount(no) }.bind()
        val closedOrError = Account.close(account, closeDate ?: today())
        createOrUpdate(this@close, closedOrError).bind()
    }

    private sealed class DC {
        object D : DC()
        object C : DC()
    }

    override suspend fun AccountRepository.debit(
        no: String, amount: Amount
    ): Either<AccountServiceException, Account> = up(this, no, amount, DC.D)

    override suspend fun AccountRepository.credit(
        no: String,
        amount: Amount
    ): Either<AccountServiceException, Account> = up(this, no, amount, DC.C)

    private suspend fun up(
        repo: AccountRepository,
        no: String,
        amount: Amount,
        dc: DC
    ): Either<AccountServiceException, Account> = either {
        val accountOrNull = repo.query(no).mapLeft { MiscellaneousDomainExceptions(it) }.bind()
        val account = accountOrNull.rightIfNotNull { NonExistingAccount(no) }.bind()
        val updated = when (dc) {
            DC.D -> createOrUpdate(repo, Account.updateBalance(account, -amount))
            DC.C -> createOrUpdate(repo, Account.updateBalance(account, amount))
        }.bind()
        updated
    }

    override suspend fun AccountRepository.balance(no: String): Either<AccountServiceException, Balance> =
        balance(no).mapLeft { MiscellaneousDomainExceptions(it) }
}