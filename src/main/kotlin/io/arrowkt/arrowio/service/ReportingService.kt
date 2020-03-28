package io.arrowkt.arrowio.service

import arrow.fx.IOPartialOf
import arrow.mtl.Kleisli
import io.arrowkt.arrowio.repository.AccountRepository

typealias ReportOperation<A> = Kleisli<AccountRepository, IOPartialOf<AccountServiceException>, A>

interface ReportingService<Amount> {

    fun balanceByAccount(): ReportOperation<Sequence<Pair<String, Amount>>>
}