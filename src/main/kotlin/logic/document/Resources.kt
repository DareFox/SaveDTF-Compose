package logic.document

import util.readResource

object Resources {
    val imageLoadFail = BinaryMedia(
        type = "image",
        subtype = "png",
        binary = readResource("img/download/imageLoadFail.png").readBytes()
    )

    val imageVideoFail = BinaryMedia(
        type = "image",
        subtype = "png",
        binary = readResource("img/download/videoLoadFail.png").readBytes()
    )
}
