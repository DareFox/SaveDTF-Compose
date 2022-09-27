package logic.io

import java.io.OutputStream

class CounterOutputStream(private val stream: OutputStream) : OutputStream() {
    private var _counter = 0L
    val counter
        get() = _counter

    override fun close() {
        stream.close()
    }

    override fun flush() {
        stream.flush()
    }

    override fun write(b: Int) {
        stream.write(b)
        _counter++
    }

    override fun write(b: ByteArray) {
        stream.write(b)
        _counter += b.size
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        stream.write(b, off, len)
        _counter += len
    }
}

fun OutputStream.asCounter(): CounterOutputStream {
    return CounterOutputStream(this)
}