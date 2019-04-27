package io.arrowkt.service

import arrow.data.EitherT
import arrow.data.Kleisli
import arrow.effects.ForIO
import io.arrowkt.repository.AccountRepository

typealias Valid<A> = EitherT<ForIO, AccountServiceException, A>

typealias AccountOperation<A> = Kleisli<Valid<A>, AccountRepository, A>