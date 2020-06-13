package tagless.service

import arrow.mtl.Kleisli
import tagless.repository.AccountRepository

interface ReportingService<F, Amount> {

    fun balanceByAccount(): Kleisli<F, AccountRepository<F>, Sequence<Pair<String, Amount>>>
}