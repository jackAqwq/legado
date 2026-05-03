package io.legado.app.ui.minireader.paging

data class MiniPaginationConfig(
    val pageCharCapacity: Int,
    val lineSpacingMultiplier: Float
)

data class MiniPageSnapshot(
    val index: Int,
    val chapterIndex: Int,
    val title: String,
    val text: String,
    val startOffset: Int,
    val endOffset: Int
)

data class MiniPaginationSnapshot(
    val pages: List<MiniPageSnapshot>,
    val current: MiniPageSnapshot,
    val prev: MiniPageSnapshot?,
    val next: MiniPageSnapshot?,
    val config: MiniPaginationConfig
)
