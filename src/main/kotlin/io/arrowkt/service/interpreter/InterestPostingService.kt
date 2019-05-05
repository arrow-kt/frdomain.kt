package io.arrowkt.service.interpreter

import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import arrow.data.EitherT
import arrow.data.EitherTPartialOf
import arrow.data.Kleisli
import arrow.effects.ForIO
import arrow.effects.IO
import io.arrowkt.Amount
import io.arrowkt.model.Account
import io.arrowkt.service.AccountServiceException
import io.arrowkt.service.ClosedAccount
import io.arrowkt.service.InterestPostingService
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