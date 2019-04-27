package io.arrowkt.service

import arrow.data.EitherTPartialOf
import arrow.data.Kleisli
import arrow.effects.ForIO
import io.arrowkt.repository.AccountRepository

typealias AccountOperation<A> = Kleisli<EitherTPartialOf<ForIO, AccountServiceException>, AccountRepository, A>