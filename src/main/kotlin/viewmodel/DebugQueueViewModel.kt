package viewmodel

import viewmodel.queue.EntryQueueElementViewModel
import viewmodel.queue.IQueueElementViewModel

object DebugQueueViewModel {
    val startQueue = listOf<IQueueElementViewModel>(
        EntryQueueElementViewModel("https://dtf.ru/apitest/1296731-sohranenie-stati-test-2")
    )
}