package io.arrowkt.frdomain.service

import arrow.core.NonEmptyList
import arrow.core.nonEmptyListOf


sealed class AccountServiceException(open val message: NonEmptyList<String>)

data class AlreadyExistingAccount(val no: String) :
    AccountServiceException(nonEmptyListOf("Already existing account with no $no"))

data class NonExistingAccount(val no: String) : AccountServiceException(nonEmptyListOf("No existing account with no $no"))

data class ClosedAccount(val no: String) : AccountServiceException(nonEmptyListOf("Account with $no already closed!"))

object RateMissingForSavingsAccount :
    AccountServiceException(nonEmptyListOf("Rate needs to be given for savings account"))

data class MiscellaneousDomainExceptions(override val message: NonEmptyList<String>) : AccountServiceException(message)