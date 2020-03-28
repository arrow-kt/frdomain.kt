package io.arrowkt.arrowio

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.ValidatedNel
import java.math.BigDecimal
import java.time.LocalDate

typealias Amount = BigDecimal
typealias ValidationResult<A> = ValidatedNel<String, A>
typealias ErrorOr<A> = Either<NonEmptyList<String>, A>
typealias DomainError = NonEmptyList<String>

fun today() = LocalDate.now()