package shared.document

import kmtt.models.entry.Entry
import kotlinx.coroutines.flow.StateFlow
import shared.abstracts.AbstractProgress
import shared.abstracts.IProgress
import shared.document.operations.*
import org.jsoup.nodes.Document

/**
 *  Operation for document processing. Used in [DocumentProcessor].
 *
 *  Example operations: [Remove all CSS styles][RemoveCssOperation], [Format HTML][FormatHtmlOperation] and etc.
 */
sealed interface IProcessorOperation : IProgress {
    /**
     * Name of operation
     */
    val name: String

    /**
     * Progress of running operation. **null** if operation is done
     */
    override val progress: StateFlow<String?>

    /**
     *  Process given document and output a new one
     */
    suspend fun process(document: Document, entry: Entry?): Document
}

/**
 * Abstract interface that implements some [IProcessorOperation] functionality
 */
abstract class AbstractProcessorOperation : IProcessorOperation, AbstractProgress()