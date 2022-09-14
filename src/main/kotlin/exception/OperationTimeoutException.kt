package exception

import ui.i18n.Lang

data class OperationTimeoutException(val timeout: Long, val operationName: String) :
    QueueElementException(
        if (timeout < 0) {
            Lang.value.exceptionOperationTimeoutMessageImmediately.format(operationName, timeout)
        } else {
            Lang.value.exceptionOperationTimeoutMessage.format(operationName, timeout)
        }
    )