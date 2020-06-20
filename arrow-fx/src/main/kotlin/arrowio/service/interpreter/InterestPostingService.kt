package arrowio.service.interpreter

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import arrowio.Amount
import arrowio.model.Account
import arrowio.service.AccountServiceException
import arrowio.service.ClosedAccount
import arrowio.service.InterestPostingService
import java.math.BigDecimal

object InterestPostingService : InterestPostingService<Account, Amount> {

    override suspend fun Account.calculateInterest(): Either<AccountServiceException, Amount> =
        if (dateOfClose != null) ClosedAccount(no).left()
        else {
            val rate = Account.rate(this)
            if (rate != null) {
                val a = balance.amount
                (a + a * rate).right()
            } else BigDecimal.ZERO.right()
        }

    override suspend fun Amount.computeTax(): Either<AccountServiceException, Amount> =
        this.multiply(BigDecimal.valueOf(0.1)).right()
}