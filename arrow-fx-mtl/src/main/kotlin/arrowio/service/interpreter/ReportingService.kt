package arrowio.service.interpreter

import arrow.core.left
import arrow.core.right
import arrow.mtl.EitherT
import arrow.mtl.Kleisli
import arrowio.Amount
import arrowio.service.MiscellaneousDomainExceptions
import arrowio.service.ReportOperation
import arrowio.service.ReportingService

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