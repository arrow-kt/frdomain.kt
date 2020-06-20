package arrowio.service

import arrow.core.NonEmptyList


sealed class AccountServiceException(open val message: NonEmptyList<String>)

data class AlreadyExistingAccount(val no: String) :
    AccountServiceException(NonEmptyList("Already existing account with no $no"))

data class NonExistingAccount(val no: String) : AccountServiceException(NonEmptyList("No existing account with no $no"))

data class ClosedAccount(val no: String) : AccountServiceException(NonEmptyList("Account with $no already closed!"))

object RateMissingForSavingsAccount :
    AccountServiceException(NonEmptyList("Rate needs to be given for savings account"))

data class MiscellaneousDomainExceptions(override val message: NonEmptyList<String>) : AccountServiceException(message)