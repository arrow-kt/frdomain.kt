package arrowio.repository.interpreter

import arrow.core.right
import arrowio.ErrorOr
import arrowio.model.Account
import arrowio.repository.AccountRepository
import arrowio.today
import java.time.LocalDate

class AccountInMemoryRepository : AccountRepository {

    private val repo by lazy { mutableMapOf<String, Account>() }

    override suspend fun query(no: String): ErrorOr<Account?> = repo[no].right()

    override suspend fun store(a: Account): ErrorOr<Account> =
        a.right().also { repo[a.no] = a }

    override suspend fun query(openedOn: LocalDate): ErrorOr<Sequence<Account>> =
        repo.values
            .asSequence()
            .filter { it.dateOfOpen ?: today() == openedOn }
            .right()

    override suspend fun all(): ErrorOr<Sequence<Account>> =
        repo.values
            .asSequence()
            .right()
}