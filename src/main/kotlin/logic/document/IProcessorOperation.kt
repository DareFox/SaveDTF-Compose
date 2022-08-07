package logic.document

import kotlinx.coroutines.flow.StateFlow
import logic.abstracts.AbstractProgress
import logic.abstracts.IProgress
import logic.document.operations.*
import org.jsoup.nodes.Document

/**
 *  Operation for document processing. Used in [DocumentProcessor].
 *
 *  Example operations: [Remove all CSS styles][RemoveCssOperation], [Format HTML][FormatHtmlOperation] and etc.
 */
sealed interface IProcessorOperation: IProgress {
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
    suspend fun process(document: Document): Document
}

/**
 * Abstract interface that implements some [IProcessorOperation] functionality
 */
abstract class AbstractProcessorOperation : IProcessorOperation, AbstractProgress()