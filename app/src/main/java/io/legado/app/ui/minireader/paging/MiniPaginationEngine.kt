package io.legado.app.ui.minireader.paging

import io.legado.app.ui.minireader.chapter.MiniChapter
import kotlin.math.floor
import kotlin.math.max

class MiniPaginationEngine {

    private var latestChapters: List<MiniChapter> = emptyList()

    fun paginate(chapters: List<MiniChapter>, config: MiniPaginationConfig): MiniPaginationSnapshot {
        latestChapters = chapters
        val pages = buildPages(chapters, config)
        return buildSnapshot(pages, 0, config)
    }

    fun next(snapshot: MiniPaginationSnapshot): MiniPaginationSnapshot {
        val targetIndex = minOf(snapshot.current.index + 1, snapshot.pages.lastIndex)
        return buildSnapshot(snapshot.pages, targetIndex, snapshot.config)
    }

    fun prev(snapshot: MiniPaginationSnapshot): MiniPaginationSnapshot {
        val targetIndex = max(snapshot.current.index - 1, 0)
        return buildSnapshot(snapshot.pages, targetIndex, snapshot.config)
    }

    fun reflowByGlobalOffset(oldOffset: Int, config: MiniPaginationConfig): MiniPaginationSnapshot {
        val pages = buildPages(latestChapters, config)
        val targetIndex = findPageIndexByOffset(pages, oldOffset)
        return buildSnapshot(pages, targetIndex, config)
    }

    private fun buildPages(
        chapters: List<MiniChapter>,
        config: MiniPaginationConfig
    ): List<MiniPageSnapshot> {
        val pages = mutableListOf<MiniPageSnapshot>()
        val capacity = resolveCapacity(config)
        chapters.forEachIndexed { chapterIndex, chapter ->
            if (chapter.content.isEmpty()) {
                return@forEachIndexed
            }
            var localOffset = 0
            while (localOffset < chapter.content.length) {
                val endExclusive = minOf(localOffset + capacity, chapter.content.length)
                val chunk = chapter.content.substring(localOffset, endExclusive)
                val globalStart = chapter.startOffset + localOffset
                val globalEnd = globalStart + chunk.length
                pages += MiniPageSnapshot(
                    index = pages.size,
                    chapterIndex = chapterIndex,
                    title = chapter.title,
                    text = chunk,
                    startOffset = globalStart,
                    endOffset = globalEnd
                )
                localOffset = endExclusive
            }
        }

        if (pages.isNotEmpty()) {
            return pages
        }

        val fallbackTitle = chapters.firstOrNull()?.title ?: "正文"
        return listOf(
            MiniPageSnapshot(
                index = 0,
                chapterIndex = 0,
                title = fallbackTitle,
                text = "",
                startOffset = 0,
                endOffset = 0
            )
        )
    }

    private fun resolveCapacity(config: MiniPaginationConfig): Int {
        val base = max(config.pageCharCapacity, 1)
        val spacing = config.lineSpacingMultiplier.takeIf { it > 0f } ?: 1f
        val scaled = floor(base / spacing).toInt()
        return max(scaled, 1)
    }

    private fun buildSnapshot(
        pages: List<MiniPageSnapshot>,
        currentIndex: Int,
        config: MiniPaginationConfig
    ): MiniPaginationSnapshot {
        val safeIndex = currentIndex.coerceIn(0, pages.lastIndex)
        val current = pages[safeIndex]
        return MiniPaginationSnapshot(
            pages = pages,
            current = current,
            prev = pages.getOrNull(safeIndex - 1),
            next = pages.getOrNull(safeIndex + 1),
            config = config
        )
    }

    private fun findPageIndexByOffset(pages: List<MiniPageSnapshot>, offset: Int): Int {
        if (pages.isEmpty()) {
            return 0
        }
        if (offset <= pages.first().startOffset) {
            return 0
        }
        pages.forEachIndexed { index, page ->
            if (page.startOffset <= offset && offset < page.endOffset) {
                return index
            }
        }
        return pages.lastIndex
    }
}
