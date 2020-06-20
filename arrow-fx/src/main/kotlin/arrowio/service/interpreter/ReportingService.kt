package arrowio.service.interpreter

import arrow.core.Either
import arrow.core.either
import arrowio.Amount
import arrowio.repository.AccountRepository
import arrowio.service.AccountServiceException
import arrowio.service.MiscellaneousDomainExceptions
import arrowio.service.ReportingService

object ReportingService : ReportingService<AccountRepository, Amount> {

    override suspend fun AccountRepository.balanceByAccount(): Either<AccountServiceException, Sequence<Pair<String, Amount>>> =
        either {
            val sequence = all().mapLeft { MiscellaneousDomainExceptions(it) }.bind()
            sequence.map { Pair(it.no, it.balance.amount) }
        }
}