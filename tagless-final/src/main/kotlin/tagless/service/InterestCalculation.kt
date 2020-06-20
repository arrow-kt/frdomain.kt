package tagless.service

import arrow.mtl.Kleisli

interface InterestCalculation<F, Account, Amount> {

    fun computeInterest(): Kleisli<Account, F, Amount>
}