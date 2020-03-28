package io.arrowkt.arrowio.repository

import arrow.core.NonEmptyList
import arrow.core.Option
import arrow.fx.IO
import arrow.fx.flatMap
import io.arrowkt.arrowio.DomainError
import io.arrowkt.arrowio.model.Account
import io.arrowkt.arrowio.model.Balance
import java.time.LocalDate

interface AccountRepository {
    fun query(no: String): IO<DomainError, Option<Account>>
    fun store(a: Account): IO<DomainError, Account>
    fun query(openedOn: LocalDate): IO<DomainError, Sequence<Account>>
    fun all(): IO<DomainError, Sequence<Account>>

    fun balance(no: String): IO<DomainError, Balance> = query(no)
        .flatMap { ao ->
            ao.fold(
                ifEmpty = { IO.raiseError<DomainError, Balance>(NonEmptyList.of("Account not found with $no")) },
                ifSome = { IO.just(it.balance) }
            )
        }
}