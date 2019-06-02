package io.arrowkt.arrowio.service

import arrow.data.EitherTPartialOf
import arrow.data.Kleisli
import arrow.effects.ForIO
import io.arrowkt.arrowio.repository.AccountRepository

typealias ReportOperation<A> = Kleisli<EitherTPartialOf<ForIO, AccountServiceException>, AccountRepository, A>

interface ReportingService<Amount> {

    fun balanceByAccount(): ReportOperation<Sequence<Pair<String, Amount>>>
}