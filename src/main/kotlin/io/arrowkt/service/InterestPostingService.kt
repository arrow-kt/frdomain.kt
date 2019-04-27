package io.arrowkt.service

interface InterestPostingService<Account, Amount> : InterestCalculation<Account, Amount>, TaxCalculation<Amount>