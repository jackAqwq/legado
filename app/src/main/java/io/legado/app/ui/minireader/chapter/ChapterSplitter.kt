package io.legado.app.ui.minireader.chapter

data class MiniChapter(
    val title: String,
    val content: String,
    val startOffset: Int,
    val endOffset: Int
)

object ChapterSplitter {

    private val titlePattern = Regex(
        "^(?:序章|前言|楔子|后记|尾声|番外|第[0-9零〇一二三四五六七八九十百千万两]+[章节卷回部篇集][^\\r\\n]{0,30})$"
    )

    fun split(text: String): List<MiniChapter> {
        val normalized = text
            .replace("\r\n", "\n")
            .replace('\r', '\n')
            .trim()
        if (normalized.isBlank()) {
            return listOf(MiniChapter("正文", "", 0, 0))
        }

        val lines = normalized.split('\n')
        val titleLineIndices = lines.mapIndexedNotNull { index, line ->
            if (isLikelyTitleLine(line.trim())) index else null
        }
        if (titleLineIndices.isEmpty()) {
            return listOf(MiniChapter("正文", normalized, 0, normalized.length))
        }

        val lineStartOffsets = IntArray(lines.size)
        var currentOffset = 0
        for (index in lines.indices) {
            lineStartOffsets[index] = currentOffset
            currentOffset += lines[index].length + 1
        }

        val chapters = mutableListOf<MiniChapter>()
        val firstTitleLine = titleLineIndices.first()
        if (firstTitleLine > 0) {
            appendChapter(
                chapters = chapters,
                title = "正文",
                lines = lines,
                lineStartOffsets = lineStartOffsets,
                startLineInclusive = 0,
                endLineInclusive = firstTitleLine - 1
            )
        }

        titleLineIndices.forEachIndexed { index, titleLine ->
            val nextTitleLine = titleLineIndices.getOrNull(index + 1) ?: lines.size
            appendChapter(
                chapters = chapters,
                title = lines[titleLine].trim(),
                lines = lines,
                lineStartOffsets = lineStartOffsets,
                startLineInclusive = titleLine + 1,
                endLineInclusive = nextTitleLine - 1
            )
        }

        if (chapters.isEmpty()) {
            return listOf(MiniChapter("正文", normalized, 0, normalized.length))
        }
        return chapters
    }

    private fun appendChapter(
        chapters: MutableList<MiniChapter>,
        title: String,
        lines: List<String>,
        lineStartOffsets: IntArray,
        startLineInclusive: Int,
        endLineInclusive: Int
    ) {
        if (startLineInclusive > endLineInclusive || startLineInclusive >= lines.size) {
            return
        }
        val safeEnd = minOf(endLineInclusive, lines.lastIndex)
        val text = lines.subList(startLineInclusive, safeEnd + 1).joinToString("\n").trim()
        if (text.isBlank()) {
            return
        }
        val startOffset = lineStartOffsets[startLineInclusive]
        val endOffset = lineStartOffsets[safeEnd] + lines[safeEnd].length
        chapters += MiniChapter(
            title = title.ifBlank { "正文" },
            content = text,
            startOffset = startOffset,
            endOffset = endOffset
        )
    }

    private fun isLikelyTitleLine(line: String): Boolean {
        if (line.isBlank() || line.length > 36) {
            return false
        }
        return titlePattern.matches(line)
    }
}
