package io.arrowkt.service.interpreter

import arrow.core.left
import arrow.core.right
import arrow.data.EitherT
import arrow.data.Kleisli
import io.arrowkt.Amount
import io.arrowkt.service.MiscellaneousDomainExceptions
import io.arrowkt.service.ReportOperation
import io.arrowkt.service.ReportingService

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