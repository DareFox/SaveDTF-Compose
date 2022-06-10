package logic.document.operations.media

import util.filesystem.validatePath

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

    fun getFileName(): String {
        return (this.metadata.key + ".${this.metadata.subtype}").validatePath()
    }
}