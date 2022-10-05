package shared.document

import org.jsoup.nodes.Document
import shared.abstracts.IProgress
import java.io.File

interface IDocumentProcessor : IProgress {
    val saveFolder: File

    // read-only for public usage
    val document: Document

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

    /**
     *  Process/transform document by [operations queue][operationsQueue]
     *
     *  @return transformed document by queue
     */
    suspend fun process(): Document

    /**
     * Adds the specified operations to the end of the operation queue
     */
    fun addOperation(vararg operations: IProcessorOperation)

    /**
     * Removes a single instance of the specified operations from operation queue, if it is present.
     *
     * @return true if any of the specified elements was removed from the queue, false if the queue was not modified
     */
    fun removeOperation(vararg operations: IProcessorOperation): Boolean

    /**
     * Removes all operations from operations queue that are also contained in the given operations collection
     *
     * @return true if any of the specified elements was removed from the queue, false if the queue was not modified
     */
    fun removeAllOperations(vararg operations: IProcessorOperation): Boolean

    /**
     * Remove all operation queue elements.
     */
    fun clearOperationsQueue()

    /**
     * Remove all previous operations and replace it with new given operations queue
     */
    fun replaceQueueWith(operations: List<IProcessorOperation>)
}