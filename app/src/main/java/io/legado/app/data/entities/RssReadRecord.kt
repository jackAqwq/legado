package io.legado.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Ignore
import kotlinx.parcelize.IgnoredOnParcel

@Entity(tableName = "rssReadRecords")
data class RssReadRecord(
    @PrimaryKey
    val record: String,
    @ColumnInfo(defaultValue = "''")
    val origin: String = "",
    @ColumnInfo(defaultValue = "''")
    val sort: String = "",
    val title: String? = null,
    val readTime: Long? = null,
    @ColumnInfo(defaultValue = "0")
    val type: Int = 0,
    val read: Boolean = true
) {

    @Ignore
    @IgnoredOnParcel
    var durPos: Int = 0

    @Ignore
    @IgnoredOnParcel
    var image: String? = null

    fun toRssArticle() = RssArticle(
        origin = origin,
        sort = sort,
        title = title ?: "",
        link = record,
        read = read
    )

    fun toStar() = toRssArticle().toStar()
}
