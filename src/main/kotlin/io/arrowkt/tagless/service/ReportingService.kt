package io.arrowkt.tagless.service

import arrow.mtl.Kleisli
import io.arrowkt.tagless.repository.AccountRepository

interface ReportingService<F, Amount> {

    fun balanceByAccount(): Kleisli<AccountRepository<F>, F, Sequence<Pair<String, Amount>>>
}