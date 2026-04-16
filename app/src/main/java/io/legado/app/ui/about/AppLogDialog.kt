package io.legado.app.ui.about

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import io.legado.app.R
import io.legado.app.base.BaseDialogFragment
import io.legado.app.base.adapter.ItemViewHolder
import io.legado.app.base.adapter.RecyclerAdapter
import io.legado.app.constant.AppLog
import io.legado.app.databinding.DialogRecyclerViewBinding
import io.legado.app.databinding.ItemAppLogBinding
import io.legado.app.help.perf.PerformanceMetricsSnapshotPresenter
import io.legado.app.help.perf.PerformanceMetricsTracker
import io.legado.app.lib.theme.primaryColor
import io.legado.app.ui.widget.dialog.TextDialog
import io.legado.app.utils.LogUtils
import io.legado.app.utils.sendToClip
import io.legado.app.utils.setLayout
import io.legado.app.utils.showDialogFragment
import io.legado.app.utils.viewbindingdelegate.viewBinding
import splitties.views.onClick
import java.util.*

class AppLogDialog : BaseDialogFragment(R.layout.dialog_recycler_view),
    Toolbar.OnMenuItemClickListener {

    private companion object {
        private const val RSS_SOURCE_READ_RSS_ACTIVITY = "ReadRssActivity"
        private const val RSS_SOURCE_BOTTOM_WEBVIEW_DIALOG = "BottomWebViewDialog"
        private const val RSS_RESULT_SUCCESS = "success"
        private const val RSS_RESULT_FAILURE = "failure"
    }

    private val binding by viewBinding(DialogRecyclerViewBinding::bind)
    private val adapter by lazy {
        LogAdapter(requireContext())
    }

    override fun onStart() {
        super.onStart()
        setLayout(0.9f, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        binding.run {
            toolBar.setBackgroundColor(primaryColor)
            toolBar.setTitle(R.string.log)
            toolBar.inflateMenu(R.menu.app_log)
            toolBar.setOnMenuItemClickListener(this@AppLogDialog)
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = adapter
        }
        adapter.setItems(AppLog.logs)
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_view_perf_metrics -> {
                showPerformanceMetrics()
            }

            R.id.menu_copy_perf_metrics -> {
                copyPerformanceMetrics()
            }

            R.id.menu_view_perf_metrics_startup -> {
                showPerformanceMetrics(
                    title = getString(R.string.performance_metrics_title_startup),
                    namePrefix = "startup."
                )
            }

            R.id.menu_view_perf_metrics_read -> {
                showPerformanceMetrics(
                    title = getString(R.string.performance_metrics_title_read),
                    namePrefix = "read."
                )
            }

            R.id.menu_view_perf_metrics_rss -> {
                showPerformanceMetrics(
                    title = getString(R.string.performance_metrics_title_rss),
                    namePrefix = "rss."
                )
            }

            R.id.menu_view_perf_metrics_rss_source_summary -> {
                showRssSourceSummary()
            }

            R.id.menu_view_perf_metrics_rss_result_summary -> {
                showRssResultSummary()
            }

            R.id.menu_view_perf_metrics_rss_source_result_summary -> {
                showRssSourceResultSummary()
            }

            R.id.menu_view_perf_metrics_rss_read_rss_activity -> {
                showPerformanceMetrics(
                    title = getString(R.string.performance_metrics_title_rss_read_rss_activity),
                    namePrefix = "rss.",
                    sourceFilter = RSS_SOURCE_READ_RSS_ACTIVITY
                )
            }

            R.id.menu_view_perf_metrics_rss_bottom_webview_dialog -> {
                showPerformanceMetrics(
                    title = getString(R.string.performance_metrics_title_rss_bottom_webview_dialog),
                    namePrefix = "rss.",
                    sourceFilter = RSS_SOURCE_BOTTOM_WEBVIEW_DIALOG
                )
            }

            R.id.menu_view_perf_metrics_rss_success -> {
                showPerformanceMetrics(
                    title = getString(R.string.performance_metrics_title_rss_success),
                    namePrefix = "rss.",
                    resultFilter = RSS_RESULT_SUCCESS
                )
            }

            R.id.menu_view_perf_metrics_rss_failure -> {
                showPerformanceMetrics(
                    title = getString(R.string.performance_metrics_title_rss_failure),
                    namePrefix = "rss.",
                    resultFilter = RSS_RESULT_FAILURE
                )
            }

            R.id.menu_view_perf_metrics_rss_failure_slowest_20 -> {
                showPerformanceMetrics(
                    title = getString(R.string.performance_metrics_title_rss_failure_slowest_20),
                    namePrefix = "rss.",
                    slowestTopLimit = 20,
                    resultFilter = RSS_RESULT_FAILURE
                )
            }

            R.id.menu_copy_perf_metrics_rss_failure_slowest_20 -> {
                copyPerformanceMetrics(
                    namePrefix = "rss.",
                    slowestTopLimit = 20,
                    resultFilter = RSS_RESULT_FAILURE
                )
            }

            R.id.menu_copy_perf_metrics_recent_20 -> {
                copyPerformanceMetrics(limit = 20)
            }

            R.id.menu_view_perf_metrics_slowest_20 -> {
                showPerformanceMetrics(
                    title = getString(R.string.performance_metrics_title_slowest_20),
                    slowestTopLimit = 20
                )
            }

            R.id.menu_copy_perf_metrics_slowest_20 -> {
                copyPerformanceMetrics(slowestTopLimit = 20)
            }

            R.id.menu_clear_perf_metrics -> {
                PerformanceMetricsTracker.clearMetrics()
            }

            R.id.menu_clear -> {
                AppLog.clear()
                adapter.clearItems()
            }
        }
        return true
    }

    private fun showPerformanceMetrics(
        title: String = getString(R.string.performance_metrics_title),
        namePrefix: String? = null,
        limit: Int? = null,
        slowestTopLimit: Int? = null,
        sourceFilter: String? = null,
        resultFilter: String? = null
    ) {
        val lines = if (slowestTopLimit != null) {
            PerformanceMetricsTracker.exportSlowLines(
                limit = slowestTopLimit,
                namePrefix = namePrefix,
                source = sourceFilter,
                result = resultFilter
            )
        } else {
            PerformanceMetricsTracker.exportLines(
                namePrefix = namePrefix,
                limit = limit,
                source = sourceFilter,
                result = resultFilter
            )
        }
        val text = PerformanceMetricsSnapshotPresenter.buildPreviewText(
            lines = lines,
            summary = if (slowestTopLimit == null) {
                PerformanceMetricsTracker.buildSummary(
                    namePrefix = namePrefix,
                    limit = limit,
                    source = sourceFilter,
                    result = resultFilter
                )
            } else {
                null
            }
        )
        showDialogFragment(TextDialog(title, text))
    }

    private fun showRssSourceSummary() {
        val text = PerformanceMetricsSnapshotPresenter.buildSourceSummaryText(
            summaries = PerformanceMetricsTracker.buildSourceSummaries(namePrefix = "rss.")
        )
        showDialogFragment(
            TextDialog(
                getString(R.string.performance_metrics_title_rss_source_summary),
                text
            )
        )
    }

    private fun showRssResultSummary() {
        val text = PerformanceMetricsSnapshotPresenter.buildResultSummaryText(
            summaries = PerformanceMetricsTracker.buildResultSummaries(namePrefix = "rss.")
        )
        showDialogFragment(
            TextDialog(
                getString(R.string.performance_metrics_title_rss_result_summary),
                text
            )
        )
    }

    private fun showRssSourceResultSummary() {
        val text = PerformanceMetricsSnapshotPresenter.buildSourceResultSummaryText(
            summaries = PerformanceMetricsTracker.buildSourceResultSummaries(namePrefix = "rss.")
        )
        showDialogFragment(
            TextDialog(
                getString(R.string.performance_metrics_title_rss_source_result_summary),
                text
            )
        )
    }

    private fun copyPerformanceMetrics(
        namePrefix: String? = null,
        limit: Int? = null,
        slowestTopLimit: Int? = null,
        sourceFilter: String? = null,
        resultFilter: String? = null
    ) {
        val lines = if (slowestTopLimit != null) {
            PerformanceMetricsTracker.exportSlowLines(
                limit = slowestTopLimit,
                namePrefix = namePrefix,
                source = sourceFilter,
                result = resultFilter
            )
        } else {
            PerformanceMetricsTracker.exportLines(
                namePrefix = namePrefix,
                limit = limit,
                source = sourceFilter,
                result = resultFilter
            )
        }
        val text = PerformanceMetricsSnapshotPresenter.buildCopyText(
            lines = lines,
            summary = if (slowestTopLimit == null) {
                PerformanceMetricsTracker.buildSummary(
                    namePrefix = namePrefix,
                    limit = limit,
                    source = sourceFilter,
                    result = resultFilter
                )
            } else {
                null
            }
        )
        requireContext().sendToClip(text)
    }

    inner class LogAdapter(context: Context) :
        RecyclerAdapter<Triple<Long, String, Throwable?>, ItemAppLogBinding>(context) {

        override fun getViewBinding(parent: ViewGroup): ItemAppLogBinding {
            return ItemAppLogBinding.inflate(inflater, parent, false)
        }

        override fun convert(
            holder: ItemViewHolder,
            binding: ItemAppLogBinding,
            item: Triple<Long, String, Throwable?>,
            payloads: MutableList<Any>
        ) {
            binding.textTime.text = LogUtils.logTimeFormat.format(Date(item.first))
            binding.textMessage.text = item.second
        }

        override fun registerListener(holder: ItemViewHolder, binding: ItemAppLogBinding) {
            binding.root.onClick {
                getItem(holder.layoutPosition)?.let { item ->
                    item.third?.let {
                        showDialogFragment(TextDialog("Log", it.stackTraceToString()))
                    }
                }
            }
        }

    }

}
