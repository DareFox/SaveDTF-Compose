package logic.document

import util.readResource

object Resources {
    val imageLoadFail = BinaryMedia(
        MediaMetadata("image", "png"),
        binary = readResource("img/download/imageLoadFail.png").readBytes()
    )

    val imageVideoFail = BinaryMedia(
        MediaMetadata("image", "png"),
        binary = readResource("img/download/videoLoadFail.png").readBytes()
    )
}
