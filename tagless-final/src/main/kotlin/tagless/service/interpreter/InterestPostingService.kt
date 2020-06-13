package tagless.service.interpreter

import arrow.core.getOrElse
import arrow.mtl.Kleisli
import arrow.typeclasses.MonadError
import tagless.Amount
import tagless.model.Account
import tagless.service.InterestPostingService
import java.math.BigDecimal

@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
class InterestPostingServiceInterpreter<F>(me: MonadError<F, Throwable>) : InterestPostingService<F, Account, Amount>,
    MonadError<F, Throwable> by me {

    override fun computeInterest(): Kleisli<Account, F, Amount> = Kleisli { account ->
        if (account.dateOfClose.isDefined()) raiseError(IllegalArgumentException("Account no ${account.no} is closed"))
        else Account.rate(account).map { r ->
            val a = account.balance.amount
            just(a + a * r)
        }.getOrElse { just(BigDecimal.ZERO) }
    }

    override fun computeTax(): Kleisli<Amount, F, Amount> = Kleisli { amount ->
        just(amount * BigDecimal("0.1"))
    }
}