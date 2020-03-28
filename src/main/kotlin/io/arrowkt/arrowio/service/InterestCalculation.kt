package io.arrowkt.arrowio.service

import arrow.fx.IOPartialOf
import arrow.mtl.Kleisli

interface InterestCalculation<Account, Amount> {

    fun calculateInterest(): Kleisli<Account, IOPartialOf<AccountServiceException>, Amount>
}