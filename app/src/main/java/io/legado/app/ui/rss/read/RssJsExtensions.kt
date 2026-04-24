package io.legado.app.ui.rss.read

import androidx.appcompat.app.AppCompatActivity
import io.legado.app.data.entities.BaseSource
import io.legado.app.help.JsExtensions
import io.legado.app.ui.association.AddToBookshelfDialog
import io.legado.app.ui.book.search.SearchActivity
import io.legado.app.utils.showDialogFragment
import java.lang.ref.WeakReference

@Suppress("unused")
open class RssJsExtensions @JvmOverloads constructor(
    activity: AppCompatActivity?,
    private val source: BaseSource? = null,
    protected val bookType: Int = 0
) : JsExtensions {

    protected val activityRef = WeakReference(activity)

    constructor(activity: ReadRssActivity) : this(activity, activity.getSource(), 0)

    override fun getSource(): BaseSource? {
        return source ?: (activityRef.get() as? ReadRssActivity)?.getSource()
    }

    fun searchBook(key: String) {
        activityRef.get()?.let {
            SearchActivity.start(it, key)
        }
    }

    fun addBook(bookUrl: String) {
        (activityRef.get() as? ReadRssActivity)?.showDialogFragment(AddToBookshelfDialog(bookUrl))
    }
}
