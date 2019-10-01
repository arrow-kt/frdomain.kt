package io.arrowkt.tagless

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.ValidatedNel
import java.math.BigDecimal
import java.time.LocalDate

typealias Amount = BigDecimal
typealias ValidationResult<A> = ValidatedNel<String, A>
typealias ErrorOr<A> = Either<NonEmptyList<String>, A>

fun today() = LocalDate.now()