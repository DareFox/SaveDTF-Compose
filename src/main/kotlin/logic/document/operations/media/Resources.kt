package logic.document.operations.media

import util.filesystem.readResource

object Resources {
    val imageLoadFail = BinaryMedia(
        MediaMetadata("image", "png", "imageLoadFail.png"),
        binary = readResource("download/imageLoadFail.png").readBytes()
    )

    val videoLoadFail = BinaryMedia(
        MediaMetadata("video", "mp4", "videoLoadFail.mp4"),
        binary = readResource("download/videoLoadFail.mp4").readBytes()
    )
}
