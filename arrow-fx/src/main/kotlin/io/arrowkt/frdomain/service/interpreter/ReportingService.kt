package io.arrowkt.frdomain.service.interpreter

import arrow.core.Either
import arrow.core.computations.either
import io.arrowkt.frdomain.Amount
import io.arrowkt.frdomain.repository.AccountRepository
import io.arrowkt.frdomain.service.AccountServiceException
import io.arrowkt.frdomain.service.MiscellaneousDomainExceptions
import io.arrowkt.frdomain.service.ReportingService

object ReportingService : ReportingService<AccountRepository, Amount> {

    override suspend fun AccountRepository.balanceByAccount(): Either<AccountServiceException, Sequence<Pair<String, Amount>>> =
        either {
            val sequence = all().mapLeft { MiscellaneousDomainExceptions(it) }.bind()
            sequence.map { Pair(it.no, it.balance.amount) }
        }
}