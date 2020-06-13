package arrowio.repository.interpreter

import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.right
import arrow.core.toOption
import arrow.fx.IO
import arrowio.ErrorOr
import arrowio.model.Account
import arrowio.repository.AccountRepository
import arrowio.today
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