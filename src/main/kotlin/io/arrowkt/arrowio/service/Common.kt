package io.arrowkt.arrowio.service

import arrow.data.EitherT
import arrow.data.EitherTPartialOf
import arrow.data.Kleisli
import arrow.data.ReaderT
import arrow.data.extensions.eithert.monad.monad
import arrow.data.extensions.kleisli.monad.monad
import arrow.effects.ForIO
import arrow.effects.IO
import arrow.effects.extensions.io.monad.monad
import io.arrowkt.arrowio.repository.AccountRepository

val AccountOperationMonad = ReaderT.monad<EitherTPartialOf<ForIO, AccountServiceException>, AccountRepository>(
    EitherT.monad(IO.monad()))

typealias AccountOperation<A> = Kleisli<EitherTPartialOf<ForIO, AccountServiceException>, AccountRepository, A>