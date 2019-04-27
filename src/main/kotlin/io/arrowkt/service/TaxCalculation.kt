package io.arrowkt.service

import arrow.data.Kleisli

interface TaxCalculation<Amount> {

    fun computeTax(): Kleisli<Valid<Amount>, Amount, Amount>
}