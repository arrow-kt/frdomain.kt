package arrowio.app

import arrow.core.Either
import arrow.core.None
import arrow.core.some
import arrow.fx.fix
import arrow.mtl.fix
import arrow.mtl.value
import arrowio.repository.interpreter.AccountInMemoryRepository
import arrowio.service.AccountOperationMonad
import arrowio.service.AccountType.CHECKING
import arrowio.service.AccountType.SAVINGS
import arrowio.service.interpreter.AccountService.credit
import arrowio.service.interpreter.AccountService.debit
import arrowio.service.interpreter.AccountService.open
import arrowio.service.interpreter.ReportingService.balanceByAccount
import java.math.BigDecimal

fun main() {
    usecase1()
    usecase2()
    usecase3()
    usecase4()
}

fun usecase1() {
    val opens = AccountOperationMonad.fx.monad {
        open("a1234", "a1name", None, None, CHECKING).bind()
        open("a2345", "a2name", None, None, CHECKING).bind()
        open("a3456", "a3name", BigDecimal(5.8).some(), None, SAVINGS).bind()
        open("a4567", "a4name", None, None, CHECKING).bind()
        open("a5678", "a5name", BigDecimal(2.3).some(), None, SAVINGS).bind()
        Unit
    }.fix()

    val credits = AccountOperationMonad.fx.monad {
        credit("a1234", BigDecimal(1000)).bind()
        credit("a2345", BigDecimal(2000)).bind()
        credit("a3456", BigDecimal(3000)).bind()
        credit("a4567", BigDecimal(4000)).bind()
        Unit
    }.fix()

    val c = AccountOperationMonad.fx.monad {
        opens.bind()
        credits.bind()
        val a = balanceByAccount().bind()
        a
    }.fix()

    val y = c.run(AccountInMemoryRepository())

    y.value().fix().unsafeRunAsync { cb ->
        cb.fold(
            { it.printStackTrace() },
            { either ->
                when (either) {
                    is Either.Left -> println(either.a)
                    is Either.Right -> either.b.forEach(::println)
                }
            }
        )
    }

//    (a1234, 1000)
//    (a2345, 2000)
//    (a3456, 3000)
//    (a4567, 4000)
//    (a5678, 0)
}

fun usecase2() {
    val c = AccountOperationMonad.fx.monad {
        open("a1234", "a1name", None, None, CHECKING).bind()
        credit("a2345", BigDecimal(2000)).bind()
        balanceByAccount().bind()
    }.fix()

    val y = c.run(AccountInMemoryRepository())

    y.value().fix().unsafeRunAsync { cb ->
        cb.fold(
            { it.printStackTrace() },
            { either ->
                when (either) {
                    is Either.Left -> println(either.a)
                    is Either.Right -> either.b.forEach(::println)
                }
            }
        )
    }

//    NonExistingAccount(no=a2345)
}

fun usecase3() {
    val c = AccountOperationMonad.fx.monad {
        open("a1234", "a1name", None, None, CHECKING).bind()
        credit("a1234", BigDecimal(2000)).bind()
        debit("a1234", BigDecimal(4000)).bind()
        balanceByAccount().bind()
    }.fix()

    val y = c.run(AccountInMemoryRepository())

    y.value().fix().unsafeRunAsync { cb ->
        cb.fold(
            { it.printStackTrace() },
            { either ->
                when (either) {
                    is Either.Left -> println(either.a)
                    is Either.Right -> either.b.forEach(::println)
                }
            }
        )
    }

//    MiscellaneousDomainExceptions(message=NonEmptyList(all=[Insufficient amount in a1234 to debit]))
}

fun usecase4() {
    val c = AccountOperationMonad.fx.monad {
        val a = open("a134", "a1name", BigDecimal(0.7).some(), None, SAVINGS).bind()
        credit(a.no, BigDecimal(2000)).bind()
        debit(a.no, BigDecimal(4000)).bind()
        balanceByAccount().bind()
    }.fix()

    val y = c.run(AccountInMemoryRepository())

    y.value().fix().unsafeRunAsync { cb ->
        cb.fold(
            { it.printStackTrace() },
            { either ->
                when (either) {
                    is Either.Left -> println(either.a)
                    is Either.Right -> either.b.forEach(::println)
                }
            }
        )
    }

//    MiscellaneousDomainExceptions(message=NonEmptyList(all=[Account No has to be at least 5 characters long: found a134]))
}