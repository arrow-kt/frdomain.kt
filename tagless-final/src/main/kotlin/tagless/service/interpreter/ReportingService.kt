package tagless.service.interpreter

import arrow.mtl.Kleisli
import arrow.typeclasses.MonadError
import tagless.Amount
import tagless.repository.AccountRepository
import tagless.service.ReportingService

@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
class ReportingServiceInterpreter<F>(me: MonadError<F, Throwable>) : ReportingService<F, Amount>,
    MonadError<F, Throwable> by me {

    override fun balanceByAccount(): Kleisli<F, AccountRepository<F>, Sequence<Pair<String, Amount>>> =
        Kleisli { repo ->
            repo.all().map { accounts -> accounts.map { Pair(it.no, it.balance.amount) } }
        }
}