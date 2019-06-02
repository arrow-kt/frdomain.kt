package io.arrowkt.arrowio.service

import arrow.core.Option
import arrow.data.fix
import java.math.BigDecimal
import java.time.LocalDate

enum class AccountType {
    CHECKING,
    SAVINGS
}

interface AccountService<Account, Amount, Balance> {

    fun open(
        no: String,
        name: String,
        rate: Option<BigDecimal>,
        openingDate: Option<LocalDate>,
        accountType: AccountType
    ): AccountOperation<Account>

    fun close(no: String, closeDate: Option<LocalDate>): AccountOperation<Account>

    fun debit(no: String, amount: Amount): AccountOperation<Account>

    fun credit(no: String, amount: Amount): AccountOperation<Account>

    fun balance(no: String): AccountOperation<Balance>

    fun transfer(from: String, to: String, amount: Amount): AccountOperation<Pair<Account, Account>> =
        AccountOperationMonad.binding {
            val a = debit(from, amount).bind()
            val b = credit(to, amount).bind()
            Pair(a, b)
        }.fix()
}