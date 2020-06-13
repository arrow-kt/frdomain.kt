package arrowio.service

import arrow.fx.ForIO
import arrow.mtl.EitherTPartialOf
import arrow.mtl.Kleisli

interface InterestCalculation<Account, Amount> {

    fun calculateInterest(): Kleisli<EitherTPartialOf<ForIO, AccountServiceException>, Account, Amount>
}