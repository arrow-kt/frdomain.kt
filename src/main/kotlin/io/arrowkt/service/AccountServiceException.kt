package io.arrowkt.service

import arrow.data.NonEmptyList

sealed class AccountServiceException(open val message: NonEmptyList<String>)

data class ClosedAccount(val no: String) :
    AccountServiceException(NonEmptyList("Account with $no already closed!"))

data class MiscellaneousDomainExceptions(override val message: NonEmptyList<String>) :
    AccountServiceException(message)