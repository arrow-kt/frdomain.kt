package io.arrowkt.tagless.rx.app

import arrow.core.none
import arrow.core.some
import arrow.fx.rx2.ForSingleK
import arrow.fx.rx2.SingleK
import arrow.fx.rx2.extensions.singlek.monadError.monadError
import arrow.fx.rx2.fix
import arrow.mtl.ReaderT
import arrow.mtl.extensions.kleisli.monad.monad
import arrow.mtl.fix
import io.arrowkt.tagless.repository.AccountRepository
import io.arrowkt.tagless.repository.interpreter.AccountRepositoryInMemory
import io.arrowkt.tagless.service.AccountType
import io.arrowkt.tagless.service.interpreter.AccountServiceInterpreter
import io.arrowkt.tagless.service.interpreter.InterestPostingServiceInterpreter
import io.arrowkt.tagless.service.interpreter.ReportingServiceInterpreter

val monadError = SingleK.monadError()
val accountServiceSingle = AccountServiceInterpreter(monadError)
val interestPostingServiceSingle = InterestPostingServiceInterpreter(monadError)
val reportingServiceSingle = ReportingServiceInterpreter(monadError)
val kleisliSingleMonad = ReaderT.monad<ForSingleK, AccountRepository<ForSingleK>>(monadError)

fun main() {
    usecase1()
    usecase2()
    usecase3()
    usecase4()
}

fun usecase1() {
    val opens = kleisliSingleMonad.fx.monad {
        accountServiceSingle.open("a1234", "a1name", none(), none(), AccountType.CHECKING).bind()
        accountServiceSingle.open("a2345", "a2name", none(), none(), AccountType.CHECKING).bind()
        accountServiceSingle.open("a3456", "a3name", 5.8.toBigDecimal().some(), none(), AccountType.SAVINGS).bind()
        accountServiceSingle.open("a4567", "a4name", none(), none(), AccountType.CHECKING).bind()
        accountServiceSingle.open("a5678", "a5name", 2.3.toBigDecimal().some(), none(), AccountType.SAVINGS).bind()
        Unit
    }

    val credits = kleisliSingleMonad.fx.monad {
        accountServiceSingle.credit("a1234", 1000.toBigDecimal()).bind()
        accountServiceSingle.credit("a2345", 2000.toBigDecimal()).bind()
        accountServiceSingle.credit("a3456", 3000.toBigDecimal()).bind()
        accountServiceSingle.credit("a4567", 4000.toBigDecimal()).bind()
        Unit
    }

    val c = kleisliSingleMonad.fx.monad {
        opens.bind()
        credits.bind()
        val a = reportingServiceSingle.balanceByAccount().bind()
        a
    }.fix()

    val y = c.run(AccountRepositoryInMemory(monadError)).fix()

    y.single.subscribe(
        { it.forEach(::println) },
        { it.printStackTrace() }
    )

//    (a1234, 1000)
//    (a2345, 2000)
//    (a3456, 3000)
//    (a4567, 4000)
//    (a5678, 0)
}

fun usecase2() {
    val c = kleisliSingleMonad.fx.monad {
        accountServiceSingle.open("a1234", "a1name", none(), none(), AccountType.CHECKING).bind()
        accountServiceSingle.credit("a2345", 2000.toBigDecimal()).bind()
        val a = reportingServiceSingle.balanceByAccount().bind()
        a
    }.fix()

    val y = c.run(AccountRepositoryInMemory(monadError)).fix()

    y.single.subscribe(
        { it.forEach(::println) },
        { it.printStackTrace() }
    )

//    java.lang.IllegalArgumentException: Account no a2345 does not exist
}

fun usecase3() {
    val c = kleisliSingleMonad.fx.monad {
        accountServiceSingle.open("a1234", "a1name", none(), none(), AccountType.CHECKING).bind()
        accountServiceSingle.credit("a1234", 2000.toBigDecimal()).bind()
        accountServiceSingle.debit("a1234", 4000.toBigDecimal()).bind()
        val a = reportingServiceSingle.balanceByAccount().bind()
        a
    }.fix()

    val y = c.run(AccountRepositoryInMemory(monadError)).fix()

    y.single.subscribe(
        { it.forEach(::println) },
        { it.printStackTrace() }
    )

//    java.lang.IllegalArgumentException: NonEmptyList(all=[Insufficient amount in a1234 to debit])
}

fun usecase4() {
    val c = kleisliSingleMonad.fx.monad {
        val a = accountServiceSingle.open("a134", "a1name", (-0.9).toBigDecimal().some(), none(), AccountType.SAVINGS).bind()
        accountServiceSingle.credit(a.no, 2000.toBigDecimal()).bind()
        accountServiceSingle.debit(a.no, 4000.toBigDecimal()).bind()
        val b = reportingServiceSingle.balanceByAccount().bind()
        b
    }.fix()

    val y = c.run(AccountRepositoryInMemory(monadError)).fix()

    y.single.subscribe(
        { it.forEach(::println) },
        { it.printStackTrace() }
    )

//    java.lang.IllegalArgumentException: NonEmptyList(all=[Interest rate -0.9 must be > 0, Account No has to be at least 5 characters long: found a134])
}