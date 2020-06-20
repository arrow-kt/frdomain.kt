package arrowio.service

import arrow.fx.ForIO
import arrow.mtl.EitherTPartialOf
import arrow.mtl.Kleisli

interface TaxCalculation<Amount> {

    fun computeTax(): Kleisli<Amount, EitherTPartialOf<AccountServiceException, ForIO>, Amount>
}