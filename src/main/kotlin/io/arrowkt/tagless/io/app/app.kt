package io.arrowkt.tagless.io.app

import arrow.core.none
import arrow.core.some
import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.io.monadError.monadError
import arrow.fx.fix
import arrow.mtl.ReaderT
import arrow.mtl.extensions.kleisli.monad.monad
import arrow.mtl.fix
import io.arrowkt.tagless.repository.AccountRepository
import io.arrowkt.tagless.repository.interpreter.AccountRepositoryInMemory
import io.arrowkt.tagless.service.AccountType
import io.arrowkt.tagless.service.interpreter.AccountServiceInterpreter
import io.arrowkt.tagless.service.interpreter.InterestPostingServiceInterpreter
import io.arrowkt.tagless.service.interpreter.ReportingServiceInterpreter

val accountServiceIO = AccountServiceInterpreter(IO.monadError())
val interestPostingServiceIO = InterestPostingServiceInterpreter(IO.monadError())
val reportingServiceIO = ReportingServiceInterpreter(IO.monadError())
val kleisliIOMonad = ReaderT.monad<ForIO, AccountRepository<ForIO>>(IO.monadError())

fun main() {
    usecase1()
    usecase2()
    usecase3()
    usecase4()
}

fun usecase1() {
    val opens = kleisliIOMonad.fx.monad {
        accountServiceIO.open("a1234", "a1name", none(), none(), AccountType.CHECKING).bind()
        accountServiceIO.open("a2345", "a2name", none(), none(), AccountType.CHECKING).bind()
        accountServiceIO.open("a3456", "a3name", 5.8.toBigDecimal().some(), none(), AccountType.SAVINGS).bind()
        accountServiceIO.open("a4567", "a4name", none(), none(), AccountType.CHECKING).bind()
        accountServiceIO.open("a5678", "a5name", 2.3.toBigDecimal().some(), none(), AccountType.SAVINGS).bind()
        Unit
    }

    val credits = kleisliIOMonad.fx.monad {
        accountServiceIO.credit("a1234", 1000.toBigDecimal()).bind()
        accountServiceIO.credit("a2345", 2000.toBigDecimal()).bind()
        accountServiceIO.credit("a3456", 3000.toBigDecimal()).bind()
        accountServiceIO.credit("a4567", 4000.toBigDecimal()).bind()
        Unit
    }

    val c = kleisliIOMonad.fx.monad {
        opens.bind()
        credits.bind()
        val a = reportingServiceIO.balanceByAccount().bind()
        a
    }.fix()

    val y = c.run(AccountRepositoryInMemory(IO.monadError())).fix()

    y.unsafeRunAsync { res ->
        res.fold(
            { it.printStackTrace() },
            { it.forEach(::println) }
        )
    }

//    (a1234, 1000)
//    (a2345, 2000)
//    (a3456, 3000)
//    (a4567, 4000)
//    (a5678, 0)
}

fun usecase2() {
    val c = kleisliIOMonad.fx.monad {
        accountServiceIO.open("a1234", "a1name", none(), none(), AccountType.CHECKING).bind()
        accountServiceIO.credit("a2345", 2000.toBigDecimal()).bind()
        val a = reportingServiceIO.balanceByAccount().bind()
        a
    }.fix()

    val y = c.run(AccountRepositoryInMemory(IO.monadError())).fix()

    y.unsafeRunAsync { res ->
        res.fold(
            { it.printStackTrace() },
            { it.forEach(::println) }
        )
    }

//    java.lang.IllegalArgumentException: Account no a2345 does not exist
}

fun usecase3() {
    val c = kleisliIOMonad.fx.monad {
        accountServiceIO.open("a1234", "a1name", none(), none(), AccountType.CHECKING).bind()
        accountServiceIO.credit("a1234", 2000.toBigDecimal()).bind()
        accountServiceIO.debit("a1234", 4000.toBigDecimal()).bind()
        val a = reportingServiceIO.balanceByAccount().bind()
        a
    }.fix()

    val y = c.run(AccountRepositoryInMemory(IO.monadError())).fix()

    y.unsafeRunAsync { res ->
        res.fold(
            { it.printStackTrace() },
            { it.forEach(::println) }
        )
    }

//    java.lang.IllegalArgumentException: NonEmptyList(all=[Insufficient amount in a1234 to debit])
}

fun usecase4() {
    val c = kleisliIOMonad.fx.monad {
        val a = accountServiceIO.open("a134", "a1name", (-0.9).toBigDecimal().some(), none(), AccountType.SAVINGS).bind()
        accountServiceIO.credit(a.no, 2000.toBigDecimal()).bind()
        accountServiceIO.debit(a.no, 4000.toBigDecimal()).bind()
        val b = reportingServiceIO.balanceByAccount().bind()
        b
    }.fix()

    val y = c.run(AccountRepositoryInMemory(IO.monadError())).fix()

    y.unsafeRunAsync { res ->
        res.fold(
            { it.printStackTrace() },
            { it.forEach(::println) }
        )
    }

//    java.lang.IllegalArgumentException: NonEmptyList(all=[Interest rate -0.9 must be > 0, Account No has to be at least 5 characters long: found a134])
}