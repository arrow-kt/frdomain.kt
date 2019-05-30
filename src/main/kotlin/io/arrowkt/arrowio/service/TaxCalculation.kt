package io.arrowkt.arrowio.service

import arrow.data.EitherTPartialOf
import arrow.data.Kleisli
import arrow.effects.ForIO

interface TaxCalculation<Amount> {

    fun computeTax(): Kleisli<EitherTPartialOf<ForIO, AccountServiceException>, Amount, Amount>
}