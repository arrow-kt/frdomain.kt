package io.arrowkt.arrowio.repository.interpreter

import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.right
import arrow.core.toOption
import arrow.effects.IO
import io.arrowkt.tagless.ErrorOr
import io.arrowkt.tagless.model.Account
import io.arrowkt.arrowio.repository.AccountRepository
import io.arrowkt.tagless.today
import java.time.LocalDate

class AccountInMemoryRepository : AccountRepository {

    private val repo by lazy { mutableMapOf<String, Account>() }

    override fun query(no: String): IO<ErrorOr<Option<Account>>> =
        IO { repo[no].toOption().right() }

    override fun store(a: Account): IO<ErrorOr<Account>> =
        IO { a.right().also { repo[a.no] = a } }

    override fun query(openedOn: LocalDate): IO<ErrorOr<Sequence<Account>>> =
        IO {
            repo.values
                .asSequence()
                .filter { it.dateOfOpen.getOrElse { today() } == openedOn }
                .right()
        }

    override fun all(): IO<ErrorOr<Sequence<Account>>> =
        IO { repo.values.asSequence().right() }
}