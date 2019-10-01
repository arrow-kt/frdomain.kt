package io.arrowkt.tagless.service

import arrow.mtl.Kleisli

interface InterestCalculation<F, Account, Amount> {

    fun computeInterest(): Kleisli<F, Account, Amount>
}