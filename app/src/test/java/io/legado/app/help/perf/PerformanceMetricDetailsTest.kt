package io.legado.app.help.perf

import org.junit.Assert.assertEquals
import org.junit.Test

class PerformanceMetricDetailsTest {

    @Test
    fun encode_should_join_entries_in_insertion_order() {
        val details = PerformanceMetricDetails.of(
            "source" to "ReadRssActivity",
            "result" to "failure",
            "status" to "500"
        )

        assertEquals(
            "source=ReadRssActivity,result=failure,status=500",
            details.encode()
        )
    }

    @Test
    fun parse_should_restore_values_from_encoded_text() {
        val details = PerformanceMetricDetails.parse(
            "source=BottomWebViewDialog,result=success,contentType=text/html"
        )

        assertEquals("BottomWebViewDialog", details["source"])
        assertEquals("success", details["result"])
        assertEquals("text/html", details["contentType"])
    }

    @Test
    fun parse_should_ignore_blank_segments_and_missing_values() {
        val details = PerformanceMetricDetails.parse("source=ReadRssActivity,,broken,result=")

        assertEquals("ReadRssActivity", details["source"])
        assertEquals(null, details["broken"])
        assertEquals(null, details["result"])
    }
}
