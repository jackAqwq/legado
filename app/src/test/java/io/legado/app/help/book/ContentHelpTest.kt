package io.legado.app.help.book

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentHelpTest {

    @Test
    fun resegment_normalizes_html_quote_and_colon_quote_style() {
        val output = ContentHelp.reSegment(
            content = "第一章\n张三:&quot;你好&quot;",
            chapterName = "第一章"
        )
        assertFalse(output.contains("&quot;"))
        assertTrue(output.contains("：“"))
    }

    @Test
    fun resegment_splits_adjacent_dialogue_quotes() {
        val output = ContentHelp.reSegment(
            content = "第一章\n“你好”  “再见”",
            chapterName = "第一章"
        )
        assertTrue(output.contains("”\n“"))
    }
}
