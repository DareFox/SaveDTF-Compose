package logic.document

import java.util.*

data class BinaryMedia(val metadata: MediaMetadata, val binary: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BinaryMedia

        if (metadata != other.metadata) return false
        if (!binary.contentEquals(other.binary)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = metadata.hashCode()
        result = 31 * result + binary.contentHashCode()
        return result
    }
}

fun BinaryMedia.toBase64HTML(): String {
    return "data:${metadata.type}/${metadata.subtype};base64,${Base64.getEncoder().encodeToString(binary)}"
}