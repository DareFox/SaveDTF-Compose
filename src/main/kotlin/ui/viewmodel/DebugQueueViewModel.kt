package ui.viewmodel

import kmtt.models.enums.Website
import kotlinx.coroutines.delay
import ui.viewmodel.queue.BookmarksElementViewModel
import ui.viewmodel.queue.EntryQueueElementViewModel
import ui.viewmodel.queue.IQueueElementViewModel
import ui.viewmodel.queue.QueueViewModel

object DebugQueueViewModel {
    val startQueue = listOf<IQueueElementViewModel>(
        EntryQueueElementViewModel("https://dtf.ru/apitest/1296731-sohranenie-stati-test-2")
    )
}