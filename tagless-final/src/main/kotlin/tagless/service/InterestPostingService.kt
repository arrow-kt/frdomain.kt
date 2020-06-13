package tagless.service

interface InterestPostingService<F, Account, Amount> :
    InterestCalculation<F, Account, Amount>,
    TaxCalculation<F, Amount>