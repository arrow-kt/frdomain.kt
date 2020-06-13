package tagless.model

import arrow.core.*
import arrow.core.extensions.nonemptylist.semigroup.semigroup
import arrow.core.extensions.validated.applicative.applicative
import tagless.Amount
import tagless.ErrorOr
import tagless.ValidationResult
import tagless.today
import java.math.BigDecimal
import java.time.LocalDate

data class Balance(val amount: Amount = BigDecimal.ZERO)

sealed class Account(
    open val no: String,
    open val name: String,
    open val dateOfOpen: Option<LocalDate>,
    open val dateOfClose: Option<LocalDate>,
    open val balance: Balance
) {
    companion object {
        private fun validateAccountNo(no: String): ValidationResult<String> =
            if (no.length < 5) "Account No has to be at least 5 characters long: found $no".invalidNel()
            else no.validNel()

        private fun validateOpenCloseDate(
            od: LocalDate,
            cd: Option<LocalDate>
        ): ValidationResult<Pair<Option<LocalDate>, Option<LocalDate>>> =
            cd.map { c ->
                if (c.isBefore(od)) "Close date $c cannot be earlier than open date $od".invalidNel()
                else Pair(od.some(), cd).validNel()
            }.fold(
                { Pair(od.toOption(), cd).validNel() },
                { it }
            )

        private fun validateRate(rate: BigDecimal): ValidationResult<BigDecimal> =
            if (rate < BigDecimal.ZERO) "Interest rate $rate must be > 0".invalidNel()
            else rate.validNel()

        fun checkingAccount(
            no: String,
            name: String,
            openDate: Option<LocalDate>,
            closeDate: Option<LocalDate>,
            balance: Balance
        ): ErrorOr<CheckingAccount> = ValidationResult.applicative(NonEmptyList.semigroup<String>())
            .map(
                validateAccountNo(no),
                validateOpenCloseDate(
                    openDate.getOrElse { today() },
                    closeDate
                )
            ) { (n: String, d: Pair<Option<LocalDate>, Option<LocalDate>>) ->
                CheckingAccount(n, name, d.first, d.second, balance)
            }.fix().toEither()

        fun savingsAccount(
            no: String,
            name: String,
            rate: BigDecimal,
            openDate: Option<LocalDate>,
            closeDate: Option<LocalDate>,
            balance: Balance
        ): ErrorOr<SavingsAccount> = ValidationResult.applicative(NonEmptyList.semigroup<String>())
            .map(
                validateAccountNo(no),
                validateOpenCloseDate(
                    openDate.getOrElse { today() },
                    closeDate
                ),
                validateRate(rate)
            ) { (n: String, d: Pair<Option<LocalDate>, Option<LocalDate>>, r: BigDecimal) ->
                SavingsAccount(n, name, r, d.first, d.second, balance)
            }.fix().toEither()

        private fun validateAccountAlreadyClosed(a: Account): ValidationResult<Account> =
            if (a.dateOfClose.isDefined()) "Account ${a.no} already closed".invalidNel()
            else a.validNel()

        private fun validateCloseDate(a: Account, cd: LocalDate): ValidationResult<LocalDate> =
            a.dateOfOpen.fold(
                { "Account ${a.no} has no open date".invalidNel() },
                { od ->
                    if (cd.isBefore(od)) "Close date $cd cannot be earlier than open date $od".invalidNel()
                    else cd.validNel()
                }
            )

        fun close(a: Account, closeDate: LocalDate): ErrorOr<Account> =
            ValidationResult.applicative(NonEmptyList.semigroup<String>())
                .map(
                    validateAccountAlreadyClosed(a),
                    validateCloseDate(a, closeDate)
                ) { (acc, _) ->
                    when (acc) {
                        is CheckingAccount -> acc.copy(dateOfClose = closeDate.some())
                        is SavingsAccount -> acc.copy(dateOfClose = closeDate.some())
                    }
                }.fix().toEither()

        private fun checkBalance(a: Account, amount: Amount): ValidationResult<Account> =
            if (amount < BigDecimal.ZERO && a.balance.amount < -amount) "Insufficient amount in ${a.no} to debit".invalidNel()
            else a.validNel()

        fun updateBalance(a: Account, amount: Amount): ErrorOr<Account> =
            ValidationResult.applicative(NonEmptyList.semigroup<String>())
                .map(
                    validateAccountAlreadyClosed(a),
                    checkBalance(a, amount)
                ) { (acc, _) ->
                    when (acc) {
                        is CheckingAccount -> acc.copy(balance = Balance(
                            acc.balance.amount.add(
                                amount
                            )
                        )
                        )
                        is SavingsAccount -> acc.copy(balance = Balance(
                            acc.balance.amount.add(
                                amount
                            )
                        )
                        )
                    }
                }.fix().toEither()

        fun rate(a: Account): Option<Amount> = when (a) {
            is SavingsAccount -> a.rateOfInterest.some()
            else -> none()
        }
    }
}

data class CheckingAccount(
    override val no: String,
    override val name: String,
    override val dateOfOpen: Option<LocalDate>,
    override val dateOfClose: Option<LocalDate> = none(),
    override val balance: Balance = Balance()
) : Account(no, name, dateOfOpen, dateOfClose, balance)

data class SavingsAccount(
    override val no: String,
    override val name: String,
    val rateOfInterest: Amount,
    override val dateOfOpen: Option<LocalDate>,
    override val dateOfClose: Option<LocalDate> = none(),
    override val balance: Balance = Balance()
) : Account(no, name, dateOfOpen, dateOfClose, balance)