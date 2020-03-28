package io.arrowkt.tagless.service

import arrow.core.Option
import arrow.mtl.Kleisli
import io.arrowkt.tagless.repository.AccountRepository
import java.math.BigDecimal
import java.time.LocalDate

enum class AccountType {
    CHECKING,
    SAVINGS
}

interface AccountService<F, Account, Amount, Balance> {

    fun open(
        no: String,
        name: String,
        rate: Option<BigDecimal>,
        openingDate: Option<LocalDate>,
        accountType: AccountType
    ): Kleisli<AccountRepository<F>, F, Account>

    fun close(no: String, closeDate: Option<LocalDate>): Kleisli< AccountRepository<F>, F, Account>

    fun debit(no: String, amount: Amount): Kleisli<AccountRepository<F>, F,  Account>

    fun credit(no: String, amount: Amount): Kleisli<AccountRepository<F>, F, Account>

    fun balance(no: String): Kleisli<AccountRepository<F>, F, Balance>

    fun transfer(from: String, to: String, amount: Amount): Kleisli<AccountRepository<F>, F, Pair<Account, Account>>
}