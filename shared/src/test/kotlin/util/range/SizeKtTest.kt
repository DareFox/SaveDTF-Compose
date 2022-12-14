package util.range

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import shared.util.range.size

internal class SizeKtTest {
    @Test
    fun getSize() {
        assertSize(0..-1)
        assertSize(-1..-0)
        assertSize(-2..-1)
        assertSize(-2..-2)
        assertSize(-2..-3)
        assertSize(2..3)
        assertSize(3..2)
    }

    private fun assertSize(range: IntRange) {
        val listResult = range.toList().size.toLong()
        val utilResult = range.size

        assertEquals(listResult, utilResult,
            "FAILED! $range : list=$listResult, util=$utilResult"
        )
    }
}