package io.arrowkt.service

import arrow.data.Kleisli

interface InterestCalculation<Account, Amount> {

    fun calculateInterest(): Kleisli<Valid<Amount>, Account, Amount>
}