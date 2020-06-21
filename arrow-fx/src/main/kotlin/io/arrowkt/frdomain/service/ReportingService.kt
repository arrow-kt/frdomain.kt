package io.arrowkt.frdomain.service

import arrow.core.Either
import io.arrowkt.frdomain.repository.AccountRepository

interface ReportingService<D, Amount> where D : AccountRepository {

    suspend fun D.balanceByAccount(): Either<AccountServiceException, Sequence<Pair<String, Amount>>>
}