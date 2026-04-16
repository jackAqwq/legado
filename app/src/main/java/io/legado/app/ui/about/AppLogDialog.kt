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
        slowestTopLimit: Int? = null
    ) {
        val lines = if (slowestTopLimit != null) {
            PerformanceMetricsTracker.exportSlowLines(
                limit = slowestTopLimit,
                namePrefix = namePrefix
            )
        } else {
            PerformanceMetricsTracker.exportLines(
                namePrefix = namePrefix,
                limit = limit
            )
        }
        val text = PerformanceMetricsSnapshotPresenter.buildPreviewText(
            lines = lines,
            summary = if (slowestTopLimit == null) {
                PerformanceMetricsTracker.buildSummary(
                    namePrefix = namePrefix,
                    limit = limit
                )
            } else {
                null
            }
        )
        showDialogFragment(TextDialog(title, text))
    }

    private fun copyPerformanceMetrics(
        namePrefix: String? = null,
        limit: Int? = null,
        slowestTopLimit: Int? = null
    ) {
        val lines = if (slowestTopLimit != null) {
            PerformanceMetricsTracker.exportSlowLines(
                limit = slowestTopLimit,
                namePrefix = namePrefix
            )
        } else {
            PerformanceMetricsTracker.exportLines(
                namePrefix = namePrefix,
                limit = limit
            )
        }
        val text = PerformanceMetricsSnapshotPresenter.buildCopyText(
            lines = lines,
            summary = if (slowestTopLimit == null) {
                PerformanceMetricsTracker.buildSummary(
                    namePrefix = namePrefix,
                    limit = limit
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
