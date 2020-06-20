package arrowio.service

import arrow.core.Either
import arrowio.repository.AccountRepository

interface ReportingService<D, Amount> where D : AccountRepository {

    suspend fun D.balanceByAccount(): Either<AccountServiceException, Sequence<Pair<String, Amount>>>
}