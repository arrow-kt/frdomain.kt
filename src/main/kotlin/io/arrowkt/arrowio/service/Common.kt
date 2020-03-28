package io.arrowkt.arrowio.service

import arrow.fx.IO
import arrow.fx.IOPartialOf
import arrow.fx.extensions.io.monad.monad
import arrow.mtl.Kleisli
import arrow.mtl.ReaderT
import arrow.mtl.extensions.kleisli.monad.monad
import io.arrowkt.arrowio.repository.AccountRepository

val AccountOperationMonad =
    ReaderT.monad<AccountRepository, IOPartialOf<AccountServiceException>>(IO.monad())

typealias AccountOperation<A> = Kleisli<AccountRepository, IOPartialOf<AccountServiceException>, A>