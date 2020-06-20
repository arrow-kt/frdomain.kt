package arrowio.service

import arrow.core.Either
import arrow.core.Option
import arrow.core.either
import arrowio.repository.AccountRepository
import java.math.BigDecimal
import java.time.LocalDate

enum class AccountType {
    CHECKING,
    SAVINGS
}

interface AccountService<D, Account, Amount, Balance> where D : AccountRepository {

    suspend fun D.open(
        no: String,
        name: String,
        rate: BigDecimal?,
        openingDate: LocalDate?,
        accountType: AccountType
    ): Either<AccountServiceException, Account>

    suspend fun D.close(no: String, closeDate: LocalDate?): Either<AccountServiceException, Account>

    suspend fun D.debit(no: String, amount: Amount): Either<AccountServiceException, Account>

    suspend fun D.credit(no: String, amount: Amount): Either<AccountServiceException, Account>

    suspend fun D.balance(no: String): Either<AccountServiceException, Balance>

    suspend fun D.transfer(
        from: String,
        to: String,
        amount: Amount
    ): Either<AccountServiceException, Pair<Account, Account>> =
        either {
            val a = debit(from, amount).bind()
            val b = credit(to, amount).bind()
            Pair(a, b)
        }
}