package io.arrowkt.arrowio.service.interpreter

import arrow.fx.mapError
import arrow.mtl.Kleisli
import io.arrowkt.arrowio.Amount
import io.arrowkt.arrowio.service.MiscellaneousDomainExceptions
import io.arrowkt.arrowio.service.ReportOperation
import io.arrowkt.arrowio.service.ReportingService

object ReportingService : ReportingService<Amount> {

    override fun balanceByAccount(): ReportOperation<Sequence<Pair<String, Amount>>> =
        Kleisli { repo ->
            repo.all()
                .map { it.map { a -> Pair(a.no, a.balance.amount) } }
                .mapError { MiscellaneousDomainExceptions(it) }
        }
}