package io.arrowkt.arrowio.service

import arrow.fx.IOPartialOf
import arrow.mtl.Kleisli

interface TaxCalculation<Amount> {

    fun computeTax(): Kleisli<Amount, IOPartialOf<AccountServiceException>, Amount>
}