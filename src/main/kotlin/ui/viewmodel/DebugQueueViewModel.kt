package ui.viewmodel

import ui.viewmodel.queue.EntryQueueElementViewModel
import ui.viewmodel.queue.IQueueElementViewModel

object DebugQueueViewModel {
    val startQueue = listOf<IQueueElementViewModel>(
        EntryQueueElementViewModel("https://dtf.ru/apitest/1296731-sohranenie-stati-test-2")
    )
}