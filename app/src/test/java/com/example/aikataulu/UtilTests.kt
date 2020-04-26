package com.example.aikataulu

import org.junit.Test
import com.example.aikataulu.api.Api
import com.example.aikataulu.models.timeToString
import org.junit.Assert.*

class UtilTests {
    @Test
    fun timestamp_toString_ShouldMatchPredefinedValues() {
        val h = 60 * 60
        val m = 60

        val s1 = timeToString(0)
        assertEquals("00:00", s1)

        val s2 = timeToString(14 * h)
        assertEquals("14:00", s2)

        val s3 = timeToString(20 * h + 35 * m)
        assertEquals("20:35", s3)
    }
}
