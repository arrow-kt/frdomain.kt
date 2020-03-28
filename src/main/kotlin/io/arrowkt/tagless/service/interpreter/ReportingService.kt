package io.arrowkt.tagless.service.interpreter

import arrow.mtl.Kleisli
import arrow.typeclasses.MonadError
import io.arrowkt.tagless.Amount
import io.arrowkt.tagless.repository.AccountRepository
import io.arrowkt.tagless.service.ReportingService

@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
class ReportingServiceInterpreter<F>(me: MonadError<F, Throwable>) : ReportingService<F, Amount>,
    MonadError<F, Throwable> by me {

    override fun balanceByAccount(): Kleisli<AccountRepository<F>, F, Sequence<Pair<String, Amount>>> =
        Kleisli { repo ->
            repo.all().map { accounts -> accounts.map { Pair(it.no, it.balance.amount) } }
        }
}