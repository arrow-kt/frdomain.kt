package arrowio.service

import arrow.fx.ForIO
import arrow.mtl.EitherTPartialOf
import arrow.mtl.Kleisli
import arrowio.repository.AccountRepository

typealias ReportOperation<A> = Kleisli<EitherTPartialOf<ForIO, AccountServiceException>, AccountRepository, A>

interface ReportingService<Amount> {

    fun balanceByAccount(): ReportOperation<Sequence<Pair<String, Amount>>>
}