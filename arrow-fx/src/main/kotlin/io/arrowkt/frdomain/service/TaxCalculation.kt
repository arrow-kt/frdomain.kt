package io.arrowkt.frdomain.service

import arrow.core.Either

interface TaxCalculation<Amount> {

    suspend fun Amount.computeTax(): Either<AccountServiceException, Amount>
}