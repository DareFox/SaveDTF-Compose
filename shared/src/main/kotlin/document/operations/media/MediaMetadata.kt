package logic.document.operations.media

@kotlinx.serialization.Serializable
data class MediaMetadata(val type: String, val subtype: String, val key: String)