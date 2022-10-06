package exception

import shared.i18n.Lang

data class OperationTimeoutException(val timeout: Long, val operationName: String) :
    QueueElementException(
        if (timeout < 0) {
            Lang.exceptionOperationTimeoutMessageImmediately.format(operationName, timeout)
        } else {
            Lang.exceptionOperationTimeoutMessage.format(operationName, timeout)
        }
    )