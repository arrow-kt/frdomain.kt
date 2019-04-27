package io.arrowkt.service

import arrow.data.NonEmptyList

sealed class AccountServiceException(open val message: NonEmptyList<String>)