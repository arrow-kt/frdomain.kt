package io.arrowkt.arrowio.service

interface InterestPostingService<Account, Amount> : InterestCalculation<Account, Amount>, TaxCalculation<Amount>