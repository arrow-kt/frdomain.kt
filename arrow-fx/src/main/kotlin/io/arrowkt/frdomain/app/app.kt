package io.arrowkt.frdomain.app

import arrow.core.computations.either
import io.arrowkt.frdomain.Amount
import io.arrowkt.frdomain.repository.AccountRepository
import io.arrowkt.frdomain.repository.interpreter.AccountInMemoryRepository
import io.arrowkt.frdomain.service.AccountServiceException
import io.arrowkt.frdomain.service.AccountType.CHECKING
import io.arrowkt.frdomain.service.AccountType.SAVINGS
import io.arrowkt.frdomain.service.interpreter.AccountService.credit
import io.arrowkt.frdomain.service.interpreter.AccountService.debit
import io.arrowkt.frdomain.service.interpreter.AccountService.open
import io.arrowkt.frdomain.service.interpreter.ReportingService.balanceByAccount
import java.math.BigDecimal

suspend fun main() {
    AccountInMemoryRepository().usecase1()
    AccountInMemoryRepository().usecase2()
    AccountInMemoryRepository().usecase3()
    AccountInMemoryRepository().usecase4()
}

suspend fun AccountRepository.usecase1() {
    val opens = either<AccountServiceException, Unit> {
        open("a1234", "a1name", null, null, CHECKING).bind()
        open("a2345", "a2name", null, null, CHECKING).bind()
        open("a3456", "a3name", BigDecimal(5.8), null, SAVINGS).bind()
        open("a4567", "a4name", null, null, CHECKING).bind()
        open("a5678", "a5name", BigDecimal(2.3), null, SAVINGS).bind()
        Unit
    }

    val credits = either<AccountServiceException, Unit> {
        credit("a1234", BigDecimal(1000)).bind()
        credit("a2345", BigDecimal(2000)).bind()
        credit("a3456", BigDecimal(3000)).bind()
        credit("a4567", BigDecimal(4000)).bind()
        Unit
    }

    val result = either<AccountServiceException, Sequence<Pair<String, Amount>>> {
        credits.bind()
        opens.bind()
        balanceByAccount().bind()
    }
    result.fold(
        { println(it) },
        { it.forEach { println(it) } }
    )

    //    (a1234, 1000)
    //    (a2345, 2000)
    //    (a3456, 3000)
    //    (a4567, 4000)
    //    (a5678, 0)
}


suspend fun AccountRepository.usecase2() {
    val result = either<AccountServiceException, Sequence<Pair<String, Amount>>> {
        open("a1234", "a1name", null, null, CHECKING).bind()
        credit("a2345", BigDecimal(2000)).bind()
        balanceByAccount().bind()
    }

    result.fold(
        { println(it) },
        { it.forEach { println(it) } }
    )

    // NonExistingAccount(no=a2345)
}

suspend fun AccountRepository.usecase3() {
    val result = either<AccountServiceException, Sequence<Pair<String, Amount>>> {
        open("a1234", "a1name", null, null, CHECKING).bind()
        credit("a1234", BigDecimal(2000)).bind()
        debit("a1234", BigDecimal(4000)).bind()
        balanceByAccount().bind()
    }

    result.fold(
        { println(it) },
        { it.forEach { println(it) } }
    )

    // MiscellaneousDomainExceptions(message=NonEmptyList([Insufficient amount in a1234 to debit]))
}

suspend fun AccountRepository.usecase4() {
    val result = either<AccountServiceException, Sequence<Pair<String, Amount>>> {
        val a = open("a134", "a1name", null, null, CHECKING).bind()
        credit(a.no, BigDecimal(2000)).bind()
        debit(a.no, BigDecimal(4000)).bind()
        balanceByAccount().bind()
    }

    result.fold(
        { println(it) },
        { it.forEach { println(it) } }
    )

    // MiscellaneousDomainExceptions(message=NonEmptyList([Account No has to be at least 5 characters long: found a134]))

}
