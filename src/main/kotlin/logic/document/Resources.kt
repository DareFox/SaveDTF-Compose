package logic.document

import util.readResource

object Resources {
    val imageLoadFail = BinaryMedia(
        MediaMetadata("image", "png", "imageLoadFail.png"),
        binary = readResource("img/download/imageLoadFail.png").readBytes()
    )

    val imageVideoFail = BinaryMedia(
        MediaMetadata("image", "png", "videoLoadFail.png"),
        binary = readResource("img/download/videoLoadFail.png").readBytes()
    )
}
