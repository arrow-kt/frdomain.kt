package io.arrowkt.frdomain.model

import arrow.core.*
import io.arrowkt.frdomain.Amount
import io.arrowkt.frdomain.ErrorOr
import io.arrowkt.frdomain.ValidationResult
import io.arrowkt.frdomain.today
import java.math.BigDecimal
import java.time.LocalDate

data class Balance(val amount: Amount = BigDecimal.ZERO)

sealed class Account(
    open val no: String,
    open val name: String,
    open val dateOfOpen: LocalDate?,
    open val dateOfClose: LocalDate?,
    open val balance: Balance
) {
    companion object {

        private fun validateAccountNo(no: String): ValidationResult<String> =
            if (no.length < 5) "Account No has to be at least 5 characters long: found $no".invalidNel()
            else no.validNel()

        private fun validateOpenCloseDate(
            od: LocalDate,
            cd: LocalDate?
        ): ValidationResult<Pair<LocalDate, LocalDate?>> =
            cd?.let { c ->
                if (c.isBefore(od)) "Close date $c cannot be earlier than open date $od".invalidNel()
                else Pair(od, cd).validNel()
            } ?: Pair(od, cd).validNel()

        private fun validateRate(rate: BigDecimal): ValidationResult<BigDecimal> =
            if (rate < BigDecimal.ZERO) "Interest rate $rate must be > 0".invalidNel()
            else rate.validNel()

        fun checkingAccount(
            no: String,
            name: String,
            openDate: LocalDate?,
            closeDate: LocalDate?,
            balance: Balance
        ): ErrorOr<CheckingAccount> =
            validateAccountNo(no).zip(
                validateOpenCloseDate(
                    openDate ?: today(),
                    closeDate
                )
            ) { n, d -> CheckingAccount(n, name, d.first, d.second, balance) }.toEither()

        fun savingsAccount(
            no: String,
            name: String,
            rate: BigDecimal,
            openDate: LocalDate?,
            closeDate: LocalDate?,
            balance: Balance
        ): ErrorOr<SavingsAccount> =
                validateAccountNo(no).zip(
                    validateOpenCloseDate(
                        openDate ?: today(),
                        closeDate
                    ),
                    validateRate(rate)
                )
             { n: String, d: Pair<LocalDate?, LocalDate?>, r: BigDecimal ->
                SavingsAccount(n, name, r, d.first, d.second, balance)
            }.toEither()

        private fun validateAccountAlreadyClosed(a: Account): ValidationResult<Account> =
            if (a.dateOfClose != null) "Account ${a.no} already closed".invalidNel()
            else a.validNel()

        private fun validateCloseDate(a: Account, cd: LocalDate): ValidationResult<LocalDate> =
            if (a.dateOfOpen == null) {
                "Account ${a.no} has no open date".invalidNel()
            } else {
                if (cd.isBefore(a.dateOfOpen)) "Close date $cd cannot be earlier than open date ${a.dateOfOpen}".invalidNel()
                else cd.validNel()
            }

        fun close(a: Account, closeDate: LocalDate): ErrorOr<Account> =
                    validateAccountAlreadyClosed(a).zip(
                    validateCloseDate(a, closeDate))
                { acc, _ ->
                    when (acc) {
                        is CheckingAccount -> acc.copy(dateOfClose = closeDate)
                        is SavingsAccount -> acc.copy(dateOfClose = closeDate)
                    }
                }.toEither()

        private fun checkBalance(a: Account, amount: Amount): ValidationResult<Account> =
            if (amount < BigDecimal.ZERO && a.balance.amount < -amount) "Insufficient amount in ${a.no} to debit".invalidNel()
            else a.validNel()

        fun updateBalance(a: Account, amount: Amount): ErrorOr<Account> =
                    validateAccountAlreadyClosed(a).zip(
                    checkBalance(a, amount)) { acc, _ ->
                    when (acc) {
                        is CheckingAccount -> acc.copy(
                            balance = Balance(
                                acc.balance.amount.add(
                                    amount
                                )
                            )
                        )
                        is SavingsAccount -> acc.copy(
                            balance = Balance(
                                acc.balance.amount.add(
                                    amount
                                )
                            )
                        )
                    }
                }.toEither()

        fun rate(a: Account): Amount? = when (a) {
            is SavingsAccount -> a.rateOfInterest
            else -> null
        }
    }
}

data class CheckingAccount(
    override val no: String,
    override val name: String,
    override val dateOfOpen: LocalDate?,
    override val dateOfClose: LocalDate? = null,
    override val balance: Balance = Balance()
) : Account(no, name, dateOfOpen, dateOfClose, balance)

data class SavingsAccount(
    override val no: String,
    override val name: String,
    val rateOfInterest: Amount,
    override val dateOfOpen: LocalDate?,
    override val dateOfClose: LocalDate? = null,
    override val balance: Balance = Balance()
) : Account(no, name, dateOfOpen, dateOfClose, balance)