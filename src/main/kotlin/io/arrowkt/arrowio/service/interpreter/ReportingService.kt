package io.arrowkt.arrowio.service.interpreter

import arrow.core.left
import arrow.core.right
import arrow.data.EitherT
import arrow.data.Kleisli
import io.arrowkt.tagless.Amount
import io.arrowkt.arrowio.service.MiscellaneousDomainExceptions
import io.arrowkt.arrowio.service.ReportOperation
import io.arrowkt.arrowio.service.ReportingService

object ReportingService : ReportingService<Amount> {

    override fun balanceByAccount(): ReportOperation<Sequence<Pair<String, Amount>>> =
        Kleisli { repo ->
            EitherT(
                repo.all()
                    .map {
                        it.fold(
                            { message -> MiscellaneousDomainExceptions(message).left() },
                            { it.map { a -> Pair(a.no, a.balance.amount) }.right() }
                        )
                    }
            )
        }
}