package io.legado.app.ui.book.read.page.provider

import io.legado.app.ui.book.read.page.entities.column.BaseColumn
import io.legado.app.ui.book.read.page.entities.column.TextBaseColumn

internal data class HtmlLineJustifyResult(
    val wordSpacing: Float
)

internal object HtmlLineJustifier {

    fun justify(
        columns: MutableList<out BaseColumn>,
        lineWidth: Int
    ): HtmlLineJustifyResult? {
        if (columns.isEmpty()) {
            return null
        }
        val firstCol = columns.first()
        val lastCol = columns.last()
        val currentWidth = lastCol.end - firstCol.start
        val residualWidth = lineWidth - currentWidth
        if (residualWidth <= 0) {
            return HtmlLineJustifyResult(wordSpacing = 0f)
        }

        val spaceCount = columns.count {
            (it as? TextBaseColumn)?.charData == " "
        }
        if (spaceCount > 1) {
            val spaceIncrement = residualWidth / spaceCount
            var currentX = firstCol.start
            for (index in columns.indices) {
                val column = columns[index]
                val width = column.end - column.start
                if ((column as? TextBaseColumn)?.charData == " " && index != columns.lastIndex) {
                    column.start = currentX
                    column.end = currentX + width + spaceIncrement
                } else {
                    column.start = currentX
                    column.end = currentX + width
                }
                currentX = column.end
            }
            return HtmlLineJustifyResult(wordSpacing = spaceIncrement)
        }

        val gapCount = columns.lastIndex
        if (gapCount <= 0) {
            return HtmlLineJustifyResult(wordSpacing = 0f)
        }
        val charIncrement = residualWidth / gapCount
        var currentX = firstCol.start
        for (index in columns.indices) {
            val column = columns[index]
            val width = column.end - column.start
            column.start = currentX
            column.end = if (index != columns.lastIndex) {
                currentX + width + charIncrement
            } else {
                currentX + width
            }
            currentX = column.end
        }
        return HtmlLineJustifyResult(wordSpacing = 0f)
    }
}
