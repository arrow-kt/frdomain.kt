package io.arrowkt.arrowio.repository.interpreter

import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.toOption
import arrow.fx.IO
import io.arrowkt.arrowio.DomainError
import io.arrowkt.arrowio.model.Account
import io.arrowkt.arrowio.repository.AccountRepository
import io.arrowkt.arrowio.today
import java.time.LocalDate

class AccountInMemoryRepository : AccountRepository {

    private val repo by lazy { mutableMapOf<String, Account>() }

    override fun query(no: String): IO<DomainError, Option<Account>> =
        IO { repo[no].toOption() }

    override fun store(a: Account): IO<DomainError, Account> =
        IO { a.also { repo[a.no] = a } }

    override fun query(openedOn: LocalDate): IO<DomainError, Sequence<Account>> =
        IO {
            repo.values
                .asSequence()
                .filter { it.dateOfOpen.getOrElse { today() } == openedOn }
        }

    override fun all(): IO<DomainError, Sequence<Account>> =
        IO { repo.values.asSequence() }
}