package tagless.service

import arrow.mtl.Kleisli

interface TaxCalculation<F, Amount> {

    fun computeTax(): Kleisli<F, Amount, Amount>
}