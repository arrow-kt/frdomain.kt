package io.arrowkt.frdomain.service

import arrow.core.Either

interface InterestCalculation<Account, Amount> {

    suspend fun Account.calculateInterest(): Either<AccountServiceException, Amount>
}