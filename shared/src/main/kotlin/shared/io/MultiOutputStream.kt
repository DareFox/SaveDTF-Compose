package shared.io

import java.io.OutputStream

class MultiOutputStream(private val streams: Collection<OutputStream>) : OutputStream() {
    override fun close() {
        streams.forEach {
            it.close()
        }
    }

    override fun flush() {
        streams.forEach {
            it.flush()
        }
    }

    override fun write(b: Int) {
        streams.forEach {
            it.write(b)

        }
    }

    override fun write(b: ByteArray) {
        streams.forEach {
            it.write(b)
        }
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        streams.forEach {
            it.write(b, off, len)
        }
    }
}

fun Collection<OutputStream>.asMultiStream(): MultiOutputStream {
    return MultiOutputStream(this)
}