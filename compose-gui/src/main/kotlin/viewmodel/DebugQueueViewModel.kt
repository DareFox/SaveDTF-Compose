package viewmodel

import Downloader
object DebugQueueViewModel {
    val startQueue
        get() = listOf(
            Downloader.entry("https://dtf.ru/apitest/1296731-sohranenie-stati-test-2")
        )
}