package com.racobos.manhattan.datasource

import java.io.IOException

sealed class AppException(message: String) : IOException(message)
open class AppNeedsUpdateException(message: String = "App needs to be updated") : AppException(message)
open class BadArgumentsException(message: String = "Bad arguments") : AppException(message)
open class NetworkConnectionException(message: String = "There was a network problem") : AppException(message)
open class NotFoundException internal constructor(message: String = "No results found") : AppException(message)
open class OperationFailedException(message: String = "The operation failed") : AppException(message)
open class PermissionException(message: String = "Unauthorized operation") : AppException(message)
open class ParseException(message: String = "The provided class does not match with the model database") :
    AppException(message)

open class TimeoutException(message: String = "Timeout was thrown") : AppException(message)
open class UnknownException(message: String = "Unkown AppException") : AppException(message)