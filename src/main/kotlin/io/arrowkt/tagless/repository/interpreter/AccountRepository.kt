package io.arrowkt.tagless.repository.interpreter

import arrow.Kind
import arrow.core.*
import arrow.typeclasses.MonadError
import io.arrowkt.tagless.model.Account
import io.arrowkt.tagless.model.Balance
import io.arrowkt.tagless.repository.AccountRepository
import io.arrowkt.tagless.today
import java.time.LocalDate

@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
class AccountRepositoryInMemory<F>(me: MonadError<F, Throwable>) : AccountRepository<F>,
    MonadError<F, Throwable> by me {

    private val repo by lazy { mutableMapOf<String, Account>() }

    override fun query(no: String): Kind<F, Option<Account>> =
        just(repo[no].toOption())

    override fun store(a: Account): Kind<F, Account> {
        repo[a.no] = a
        return just(a)
    }

    override fun query(openedOn: LocalDate): Kind<F, Sequence<Account>> {
        val result = repo.values
            .asSequence()
            .filter { it.dateOfOpen.getOrElse { today() } == openedOn }
        return just(result)
    }

    override fun all(): Kind<F, Sequence<Account>> =
        just(repo.values.asSequence())

    override fun balance(no: String): Kind<F, Balance> =
        repo[no].toOption()
            .map { it.balance }
            .fold(
                { throw IllegalArgumentException("Non existing account not $no") },
                { just(it) }
            )
}