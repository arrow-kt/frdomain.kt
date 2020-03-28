package io.arrowkt.arrowio.service.interpreter

import arrow.core.getOrElse
import arrow.fx.IO
import arrow.fx.IOPartialOf
import arrow.mtl.Kleisli
import io.arrowkt.arrowio.Amount
import io.arrowkt.arrowio.model.Account
import io.arrowkt.arrowio.service.AccountServiceException
import io.arrowkt.arrowio.service.ClosedAccount
import io.arrowkt.arrowio.service.InterestPostingService
import java.math.BigDecimal

object InterestPostingService : InterestPostingService<Account, Amount> {

    override fun calculateInterest(): Kleisli<Account, IOPartialOf<AccountServiceException>, Amount> =
        Kleisli { account ->
            if (account.dateOfClose.isDefined()) IO.raiseError(ClosedAccount(account.no))
            else IO {
                Account.rate(account).map { r ->
                    val a = account.balance.amount
                    (a + a * r)
                }.getOrElse { BigDecimal.ZERO }
            }
        }

    override fun computeTax(): Kleisli<Amount, IOPartialOf<AccountServiceException>, Amount> =
        Kleisli { amount -> IO { amount.multiply(BigDecimal.valueOf(0.1)) } }
}