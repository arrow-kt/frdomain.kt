package io.arrowkt.tagless.service

import arrow.data.Kleisli

interface TaxCalculation<F, Amount> {

    fun computeTax(): Kleisli<F, Amount, Amount>
}