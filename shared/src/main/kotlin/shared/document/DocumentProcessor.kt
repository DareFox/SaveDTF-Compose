package shared.document

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.yield
import shared.abstracts.AbstractProgress
import mu.KotlinLogging
import org.jsoup.nodes.Document
import shared.util.coroutine.cancelOnSuspendEnd
import shared.util.progress.redirectTo
import java.io.File

/**
 * Process document by using [operations queue][operationsQueue]
 */
class DocumentProcessor(
    document: Document,
    override val saveFolder: File,
    operations: List<IProcessorOperation> = listOf(),
) : AbstractProgress(), IDocumentProcessor {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val logger = KotlinLogging.logger { }

    private var _document: Document = document

    // read-only for public usage
    override val document: Document
        get() = _document


    // private mutable queue
    private var _operationsQueue: MutableList<IProcessorOperation> = operations.toMutableList()

    /**
     *  Collection of [operations][IProcessorOperation] that used as queue for [transforming. processing][process] document.
     *
     *  [Processor][DocumentProcessor] uses queue from first to last
     *
     *  @see addOperation
     *  @see removeOperation
     *  @see removeAllOperations
     *  @see clearOperationsQueue
     *  @see replaceQueueWith
     */
    override val operationsQueue: List<IProcessorOperation>
        get() = _operationsQueue

    /**
     *  Process/transform document by [operations queue][operationsQueue]
     *
     *  @return transformed document by queue
     */
    override suspend fun process(): Document {
        var document = document

        operationsQueue.forEach { operation ->
            yield()
            logger.debug { "Processing document with ${operation::class.simpleName}" }
            val progressJob = operation.redirectTo(mutableProgress)
            progressJob.cancelOnSuspendEnd {
                document = operation.process(document)
            }
        }

        clearProgress()
        return document
    }

    /**
     * Adds the specified operations to the end of the operation queue
     */
    override fun addOperation(vararg operations: IProcessorOperation) {
        for (operation in operations) {
            _operationsQueue.add(operation)
        }
    }

    /**
     * Removes a single instance of the specified operations from operation queue, if it is present.
     *
     * @return true if any of the specified elements was removed from the queue, false if the queue was not modified
     */
    override fun removeOperation(vararg operations: IProcessorOperation): Boolean {
        var result = false

        for (operation in operations) {
            if (_operationsQueue.remove(operation)) {
                result = true
            }
        }

        return result
    }

    /**
     * Removes all operations from operations queue that are also contained in the given operations collection
     *
     * @return true if any of the specified elements was removed from the queue, false if the queue was not modified
     */
    override fun removeAllOperations(vararg operations: IProcessorOperation): Boolean {
        return _operationsQueue.removeAll(operations)
    }

    /**
     * Remove all operation queue elements.
     */
    override fun clearOperationsQueue() {
        _operationsQueue.clear()
    }

    /**
     * Remove all previous operations and replace it with new given operations queue
     */
    override fun replaceQueueWith(operations: List<IProcessorOperation>) {
        clearOperationsQueue()
        _operationsQueue = operations.toMutableList()
    }
}
