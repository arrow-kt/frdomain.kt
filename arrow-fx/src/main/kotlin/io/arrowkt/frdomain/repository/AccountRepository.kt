package io.arrowkt.frdomain.repository

import arrow.core.*
import io.arrowkt.frdomain.ErrorOr
import io.arrowkt.frdomain.model.Account
import io.arrowkt.frdomain.model.Balance
import java.time.LocalDate

interface AccountRepository {
    suspend fun query(no: String): ErrorOr<Account?>
    suspend fun store(a: Account): ErrorOr<Account>
    suspend fun query(openedOn: LocalDate): ErrorOr<Sequence<Account>>
    suspend fun all(): ErrorOr<Sequence<Account>>

    suspend fun balance(no: String): ErrorOr<Balance> = query(no)
        .flatMap {
            it?.balance?.right() ?: nonEmptyListOf("No account exists with $no").left()
        }
}