package logic.document

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.yield
import logic.abstracts.AbstractProgress
import mu.KotlinLogging
import org.jsoup.nodes.Document
import util.coroutine.cancelOnSuspendEnd
import util.progress.redirectTo
import java.io.File

/**
 * Process document by using [operations queue][operationsQueue]
 */
class DocumentProcessor(
    document: Document,
    val saveFolder: File,
    operations: List<IProcessorOperation> = listOf(),
) : AbstractProgress() {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val logger = KotlinLogging.logger { }

    // read-only for public usage
    var document: Document = document
        private set


    // private mutable queue
    var _operationsQueue: MutableList<IProcessorOperation> = operations.toMutableList()

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
    val operationsQueue: List<IProcessorOperation>
        get() = _operationsQueue

    /**
     *  Process/transform document by [operations queue][operationsQueue]
     *
     *  @return transformed document by queue
     */
    suspend fun process(): Document {
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
    fun addOperation(vararg operations: IProcessorOperation) {
        for (operation in operations) {
            _operationsQueue.add(operation)
        }
    }

    /**
     * Removes a single instance of the specified operations from operation queue, if it is present.
     *
     * @return true if any of the specified elements was removed from the queue, false if the queue was not modified
     */
    fun removeOperation(vararg operations: IProcessorOperation): Boolean {
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
    fun removeAllOperations(vararg operations: IProcessorOperation): Boolean {
        return _operationsQueue.removeAll(operations)
    }

    /**
     * Remove all operation queue elements.
     */
    fun clearOperationsQueue() {
        _operationsQueue.clear()
    }

    /**
     * Remove all previous operations and replace it with new given operations queue
     */
    fun replaceQueueWith(operations: List<IProcessorOperation>) {
        clearOperationsQueue()
        _operationsQueue = operations.toMutableList()
    }
}
