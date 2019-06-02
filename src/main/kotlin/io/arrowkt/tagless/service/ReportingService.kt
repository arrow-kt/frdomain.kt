package io.arrowkt.tagless.service

import arrow.data.Kleisli
import io.arrowkt.tagless.repository.AccountRepository

interface ReportingService<F, Amount> {

    fun balanceByAccount(): Kleisli<F, AccountRepository<F>, Sequence<Pair<String, Amount>>>
}