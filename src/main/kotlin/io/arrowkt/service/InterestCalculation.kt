package io.arrowkt.service

import arrow.data.EitherTPartialOf
import arrow.data.Kleisli
import arrow.effects.ForIO

interface InterestCalculation<Account, Amount> {

    fun calculateInterest(): Kleisli<EitherTPartialOf<ForIO, AccountServiceException>, Account, Amount>
}