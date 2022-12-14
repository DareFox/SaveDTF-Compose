package shared.document

import kmtt.models.entry.Entry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.yield
import shared.abstracts.AbstractProgress
import mu.KotlinLogging
import org.jsoup.nodes.Document
import shared.document.operations.OperationArguments
import shared.util.coroutine.cancelOnSuspendEnd
import shared.util.progress.redirectTo
import java.io.File

/**
 * Process document by using [operations queue][operationsQueue]
 */
class DocumentProcessor(
    override val document: Document,
    val entry: Entry?,
    override val saveFolder: File,
    operations: Set<IProcessorOperation> = setOf(),
) : AbstractProgress(), IDocumentProcessor {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val logger = KotlinLogging.logger { }

    // private mutable queue
    private var _operationsQueue: MutableSet<IProcessorOperation> = operations.toMutableSet()

    override val operationsQueue: Set<IProcessorOperation>
        get() = _operationsQueue

    override suspend fun process(): Document {
        var document = document

        operationsQueue.forEach { operation ->
            yield()
            logger.debug { "Processing document with ${operation::class.simpleName}" }
            val progressJob = operation.redirectTo(mutableProgress)
            progressJob.cancelOnSuspendEnd {
                document = operation.process(OperationArguments(document, entry, saveFolder))
            }
        }

        clearProgress()
        return document
    }

    override fun addOperation(vararg operations: IProcessorOperation) {
        for (operation in operations) {
            _operationsQueue.add(operation)
        }
    }

    override fun removeOperation(vararg operations: IProcessorOperation): Boolean {
        var result = false

        for (operation in operations) {
            if (_operationsQueue.remove(operation)) {
                result = true
            }
        }

        return result
    }

    override fun removeAllOperations(vararg operations: IProcessorOperation): Boolean {
        return _operationsQueue.removeAll(operations.toSet())
    }

    override fun clearOperationsQueue() {
        _operationsQueue.clear()
    }

    override fun replaceQueueWith(operations: Set<IProcessorOperation>) {
        clearOperationsQueue()
        _operationsQueue = operations.toMutableSet()
    }
}
