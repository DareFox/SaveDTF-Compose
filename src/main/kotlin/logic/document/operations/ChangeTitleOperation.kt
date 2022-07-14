package logic.document.operations

import androidx.compose.runtime.collectAsState
import logic.document.AbstractProcessorOperation
import org.jsoup.nodes.Document
import ui.viewmodel.SettingsViewModel

/**
 * Change title to SaveDTF
 */
object ChangeTitleOperation : AbstractProcessorOperation() {
    private val lang = SettingsViewModel.proxyLocale

    override val name: String
        get() = lang.value.titleOperation

    override suspend fun process(document: Document): Document {
        withProgress(lang.value.titleOperationProgress) {
            document.head().getElementsByTag("title").first()?.text("SaveDTF")
        }

        clearProgress()
        return document
    }
}