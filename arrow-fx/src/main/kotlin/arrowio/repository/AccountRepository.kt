package arrowio.repository

import arrow.core.NonEmptyList
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import arrowio.ErrorOr
import arrowio.model.Account
import arrowio.model.Balance
import java.time.LocalDate

interface AccountRepository {
    suspend fun query(no: String): ErrorOr<Account?>
    suspend fun store(a: Account): ErrorOr<Account>
    suspend fun query(openedOn: LocalDate): ErrorOr<Sequence<Account>>
    suspend fun all(): ErrorOr<Sequence<Account>>

    suspend fun balance(no: String): ErrorOr<Balance> = query(no)
        .flatMap {
            it?.balance?.right() ?: NonEmptyList.of("No account exists with $no").left()
        }
}