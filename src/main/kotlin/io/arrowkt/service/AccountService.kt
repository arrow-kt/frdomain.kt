package io.arrowkt.service

import arrow.core.Option
import arrow.data.EitherT
import arrow.data.EitherTPartialOf
import arrow.data.ReaderT
import arrow.data.extensions.eithert.monad.monad
import arrow.data.extensions.kleisli.monad.monad
import arrow.data.fix
import arrow.effects.ForIO
import arrow.effects.IO
import arrow.effects.extensions.io.monad.monad
import io.arrowkt.repository.AccountRepository
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
        ReaderT.monad<EitherTPartialOf<ForIO, AccountServiceException>, AccountRepository>(EitherT.monad(IO.monad())).binding {
            val a = debit(from, amount).bind()
            val b = credit(to, amount).bind()
            Pair(a, b)
        }.fix()
}