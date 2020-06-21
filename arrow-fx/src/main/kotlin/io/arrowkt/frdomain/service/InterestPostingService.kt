package io.arrowkt.frdomain.service

interface InterestPostingService<Account, Amount> :
    InterestCalculation<Account, Amount>,
    TaxCalculation<Amount>