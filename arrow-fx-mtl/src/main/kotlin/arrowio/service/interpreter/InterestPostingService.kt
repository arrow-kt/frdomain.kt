package arrowio.service.interpreter

import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import arrow.fx.ForIO
import arrow.fx.IO
import arrow.mtl.EitherT
import arrow.mtl.EitherTPartialOf
import arrow.mtl.Kleisli
import arrowio.Amount
import arrowio.model.Account
import arrowio.service.AccountServiceException
import arrowio.service.ClosedAccount
import arrowio.service.InterestPostingService
import java.math.BigDecimal

object InterestPostingService : InterestPostingService<Account, Amount> {

    override fun calculateInterest(): Kleisli<EitherTPartialOf<ForIO, AccountServiceException>, Account, Amount> =
        Kleisli { account ->
            EitherT(
                IO {
                    if (account.dateOfClose.isDefined()) ClosedAccount(account.no).left()
                    else Account.rate(account).map { r ->
                        val a = account.balance.amount
                        (a + a * r).right()
                    }.getOrElse { BigDecimal.ZERO.right() }
                }
            )
        }

    override fun computeTax(): Kleisli<EitherTPartialOf<ForIO, AccountServiceException>, Amount, Amount> =
        Kleisli { amount ->
            EitherT(
                IO {
                    amount.multiply(BigDecimal.valueOf(0.1)).right()
                }
            )
        }
}