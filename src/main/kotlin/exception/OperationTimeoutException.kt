package exception

data class OperationTimeoutException(val timeout: Long, val operationName: String) :
    QueueElementException(
        "Operation \"$operationName\" timed out ${if (timeout < 0) "immediately" else "in $timeout ms"}"
    )