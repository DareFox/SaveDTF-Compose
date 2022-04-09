package logic.document

import java.util.*

data class BinaryMedia(val type: String, val subtype: String, val binary: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BinaryMedia

        if (type != other.type) return false
        if (subtype != other.subtype) return false
        if (!binary.contentEquals(other.binary)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + subtype.hashCode()
        result = 31 * result + binary.contentHashCode()
        return result
    }
}

fun BinaryMedia.toBase64HTML(): String {
    return "data:${type}/${subtype};base64,${Base64.getEncoder().encodeToString(binary)}"
}