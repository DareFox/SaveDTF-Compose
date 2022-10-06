package shared.document.operations

import kmtt.models.entry.Entry
import org.jsoup.nodes.Document
import java.io.File

data class OperationArguments(
    val document: Document,
    val entry: Entry?,
    val saveFolder: File,
)
