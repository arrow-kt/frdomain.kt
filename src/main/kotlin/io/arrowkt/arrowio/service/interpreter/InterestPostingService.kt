package io.arrowkt.arrowio.service.interpreter

import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import arrow.data.EitherT
import arrow.data.EitherTPartialOf
import arrow.data.Kleisli
import arrow.effects.ForIO
import arrow.effects.IO
import io.arrowkt.tagless.Amount
import io.arrowkt.tagless.model.Account
import io.arrowkt.arrowio.service.AccountServiceException
import io.arrowkt.arrowio.service.ClosedAccount
import io.arrowkt.arrowio.service.InterestPostingService
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