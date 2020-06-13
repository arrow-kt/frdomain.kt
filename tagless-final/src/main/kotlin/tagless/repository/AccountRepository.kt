package tagless.repository

import arrow.Kind
import arrow.core.Option
import tagless.model.Account
import tagless.model.Balance
import java.time.LocalDate

interface AccountRepository<F> {
    fun query(no: String): Kind<F, Option<Account>>
    fun store(a: Account): Kind<F, Account>
    fun query(openedOn: LocalDate): Kind<F, Sequence<Account>>
    fun all(): Kind<F, Sequence<Account>>
    fun balance(no: String): Kind<F, Balance>
}