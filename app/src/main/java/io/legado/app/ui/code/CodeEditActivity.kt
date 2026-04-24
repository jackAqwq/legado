package io.legado.app.ui.code

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Space
import android.widget.Switch
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.github.rosemoe.sora.event.PublishSearchResultEvent
import io.github.rosemoe.sora.event.SelectionChangeEvent
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.util.regex.RegexBackrefGrammar
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.EditorSearcher
import io.github.rosemoe.sora.widget.EditorSearcher.SearchOptions
import io.legado.app.R
import io.legado.app.base.VMBaseActivity
import io.legado.app.constant.PreferKey
import io.legado.app.help.config.AppConfig
import io.legado.app.help.config.ThemeConfig
import io.legado.app.lib.dialogs.SelectItem
import io.legado.app.lib.dialogs.alert
import io.legado.app.ui.about.AppLogDialog
import io.legado.app.ui.code.config.ChangeThemeDialog
import io.legado.app.ui.code.config.SettingsDialog
import io.legado.app.ui.compose.LegadoComposeTheme
import io.legado.app.ui.widget.TitleBar
import io.legado.app.ui.widget.keyboard.KeyboardToolPop
import io.legado.app.utils.dpToPx
import io.legado.app.utils.getCompatColor
import io.legado.app.utils.imeHeight
import io.legado.app.utils.putPrefBoolean
import io.legado.app.utils.setOnApplyWindowInsetsListenerCompat
import io.legado.app.utils.showDialogFragment
import io.legado.app.utils.showHelp

class CodeEditActivity :
    VMBaseActivity<CodeEditComposeBinding, CodeEditViewModel>(),
    KeyboardToolPop.CallBack, ChangeThemeDialog.CallBack, SettingsDialog.CallBack {
    companion object {
        private var isInitialized = false
        private var findText = ""
        private var replaceText = ""
        private var isRegex = true
    }
    override val binding = CodeEditComposeBinding(this)
    override val viewModel by viewModels<CodeEditViewModel>()
    private val softKeyboardTool by lazy {
        KeyboardToolPop(this, lifecycleScope, binding.root, this)
    }
    private val editor: CodeEditor by lazy { binding.editText }
    private val editorSearcher: EditorSearcher by lazy { editor.searcher }
    private var searchOptions: SearchOptions? = null
    private var menuSaveBtn: MenuItem? = null

    private val isDark
        get() = AppConfig.editTemeAuto && ThemeConfig.isDarkTheme()
    private var themeIndex = -1

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        binding.composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        binding.composeView.setContent {
            CodeEditScreen(binding.mainContainer)
        }
        softKeyboardTool.attachToWindow(window)
        editor.colorScheme = TextMateColorScheme2.create(ThemeRegistry.getInstance())
        viewModel.initData(intent) {
            editor.apply {
                viewModel.title?.let {
                    binding.titleBar.title = it
                }
                nonPrintablePaintingFlags = AppConfig.editNonPrintable
                setEditorLanguage(viewModel.language)
                upEdit(AppConfig.editFontScale, null, AppConfig.editAutoWrap)
                setText(viewModel.initialText)
                editable = viewModel.writable
                menuSaveBtn?.isVisible = viewModel.writable
                requestFocus()
                postDelayed({
                    val pos = cursor.indexer.getCharPosition(viewModel.cursorPosition)
                    setSelection(pos.line, pos.column, true)
                }, 360)
            }
        }
        initView()
    }

    private fun initView() {
        binding.root.setOnApplyWindowInsetsListenerCompat { _, windowInsets ->
            softKeyboardTool.initialPadding = windowInsets.imeHeight
            windowInsets
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        editorSearcher.stopSearch()
        editor.release()
    }

    private fun save(check: Boolean) {
        if (!viewModel.writable) return super.finish()
        val text = editor.text.toString()
        val cursorPos = editor.cursor?.left ?: 0
        when {
            text == viewModel.initialText -> {
                if (cursorPos > 0) {
                    val result = Intent().apply {
                        putExtra("cursorPosition", cursorPos)
                    }
                    setResult(RESULT_OK, result)
                }
                super.finish()
            }
            check -> {
                alert(R.string.exit) {
                    setMessage(R.string.exit_no_save)
                    positiveButton(R.string.yes)
                    negativeButton(R.string.no) {
                        if (cursorPos > 0) {
                            val result = Intent().apply {
                                putExtra("cursorPosition", cursorPos)
                            }
                            setResult(RESULT_OK, result)
                        }
                        super.finish()
                    }
                }
            }
            else -> {
                val result = Intent().apply {
                    putExtra("text", text)
                    putExtra("cursorPosition", cursorPos)
                }
                setResult(RESULT_OK, result)
                super.finish()
            }
        }
    }

    override fun upEdit(fontSize: Int?, autoComplete: Boolean?, autoWarp: Boolean?, editNonPrintable: Int?) {
        if (fontSize != null) {
            editor.setTextSize(fontSize.toFloat())
        }
        if (autoComplete != null) {
            viewModel.language?.isAutoCompleteEnabled = autoComplete
            editor.setEditorLanguage(viewModel.language)
        }
        if (autoWarp != null) {
            editor.isWordwrap = autoWarp
        }
        if (editNonPrintable != null) {
            editor.nonPrintablePaintingFlags = editNonPrintable
        }
    }

    override fun initTheme() {
        super.initTheme()
        if (!isInitialized) {
            viewModel.initSora()
            isInitialized = true
        }
        val index = if (isDark) {
            AppConfig.editThemeDark
        } else {
            AppConfig.editTheme
        }
        upTheme(index)
        themeIndex = index
    }

    override fun upTheme(index: Int) {
        if (themeIndex != index) {
            viewModel.loadTextMateThemes(index)
            editor.setEditorLanguage(viewModel.language)
            themeIndex = index
        }
    }

    override fun onCompatCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.code_edit_activity, menu)
        menuSaveBtn = menu.findItem(R.id.menu_save).apply {
            isVisible = viewModel.writable
        }
        return super.onCompatCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.menu_auto_wrap)?.isChecked = AppConfig.editAutoWrap
        return super.onPrepareOptionsMenu(menu)
    }

    private fun setSearchOptions() {
        searchOptions = SearchOptions(
            if (isRegex) SearchOptions.TYPE_REGULAR_EXPRESSION else SearchOptions.TYPE_NORMAL,
            !isRegex,
            RegexBackrefGrammar.DEFAULT
        )
    }

    private fun search() {
        if (binding.searchGroup.isVisible) return
        binding.switchRegex.run {
            isChecked = isRegex
            setSearchOptions()
            setOnCheckedChangeListener { _, isChecked ->
                isRegex = isChecked
                setSearchOptions()
                searchTxt(binding.etFind.text.toString())
            }
        }
        val receiptSearch =
            editor.subscribeEvent(PublishSearchResultEvent::class.java) { event, _ ->
                if (event.editor == editor) {
                    updateSearchResults()
                }
            }
        val receiptChange = editor.subscribeEvent(SelectionChangeEvent::class.java) { event, _ ->
            if (event.cause == SelectionChangeEvent.CAUSE_SEARCH) {
                updateSearchResults()
            }
        }
        binding.searchGroup.visibility = View.VISIBLE
        binding.btnCloseFind.setOnClickListener {
            binding.searchGroup.visibility = View.GONE
            editorSearcher.stopSearch()
            receiptSearch.unsubscribe()
            receiptChange.unsubscribe()
            editor.requestFocus()
            editor.invalidate()
        }
        searchTxt(findText)
        binding.etFind.run {
            requestFocus()
            setText(findText)
            addTextChangedListener { text ->
                if (!text.isNullOrEmpty()) {
                    findText = text.toString()
                    searchTxt(findText)
                } else {
                    editorSearcher.stopSearch()
                    editor.invalidate()
                }
            }
        }
        binding.etReplace.run {
            setText(replaceText)
            addTextChangedListener { text ->
                if (!text.isNullOrEmpty()) {
                    replaceText = text.toString()
                }
            }
        }
        binding.btnPrevious.setOnClickListener {
            if (editorSearcher.hasQuery()) {
                editorSearcher.gotoPrevious()
            }
        }
        binding.btnNext.setOnClickListener {
            if (editorSearcher.hasQuery()) {
                editorSearcher.gotoNext()
            }
        }
        binding.btnReplace.setOnClickListener {
            if (binding.replaceGroup.isGone) {
                binding.replaceGroup.visibility = View.VISIBLE
                binding.btnReplaceAll.isEnabled = true
                binding.etReplace.requestFocus()
            } else {
                if (editorSearcher.hasQuery()) {
                    editorSearcher.replaceCurrentMatch(binding.etReplace.text.toString())
                }
            }
        }
        binding.btnCloseReplace.setOnClickListener {
            binding.replaceGroup.visibility = View.GONE
            binding.btnReplaceAll.isEnabled = false
            binding.etFind.requestFocus()
        }
        binding.btnReplaceAll.setOnClickListener {
            if (editorSearcher.hasQuery()) {
                editorSearcher.replaceAll(binding.etReplace.text.toString())
            }
        }
    }

    private fun searchTxt(txt: String) {
        if (txt.isNotEmpty()) {
            try {
                searchOptions?.let {
                    editorSearcher.search(txt, it)
                }
            } catch (_: java.util.regex.PatternSyntaxException) {
                editorSearcher.stopSearch()
                editor.invalidate()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateSearchResults() {
        if (editorSearcher.hasQuery()) {
            val totalResults = editorSearcher.matchedPositionCount
            val currentPosition = editorSearcher.currentMatchedPositionIndex + 1
            binding.tvSearchResult.text =
                "${if (currentPosition > 0) "$currentPosition/" else ""}$totalResults"
        }
    }

    override fun onCompatOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_search -> search()
            R.id.menu_save -> save(false)
            R.id.menu_format_code -> viewModel.formatCode(editor)
            R.id.menu_change_theme -> showDialogFragment(ChangeThemeDialog())
            R.id.menu_config_settings -> showDialogFragment(SettingsDialog(this, this))
            R.id.menu_auto_wrap -> {
                item.isChecked = !AppConfig.editAutoWrap
                upEdit(autoWarp = !AppConfig.editAutoWrap)
                putPrefBoolean(PreferKey.editAutoWrap, !AppConfig.editAutoWrap)
            }
            R.id.menu_log -> showDialogFragment<AppLogDialog>()
        }
        return super.onCompatOptionsItemSelected(item)
    }

    override fun finish() {
        save(true)
    }

    override fun helpActions(): List<SelectItem<String>> {
        return arrayListOf(
            SelectItem("书源教程", "ruleHelp"),
            SelectItem("订阅源教程", "rssRuleHelp"),
            SelectItem("js教程", "jsHelp"),
            SelectItem("正则教程", "regexHelp")
        )
    }

    override fun onHelpActionSelect(action: String) {
        when (action) {
            "ruleHelp" -> showHelp("ruleHelp")
            "rssRuleHelp" -> showHelp("rssRuleHelp")
            "jsHelp" -> showHelp("jsHelp")
            "regexHelp" -> showHelp("regexHelp")
        }
    }

    override fun sendText(text: String) {
        val view = window.decorView.findFocus()
        if (view is TextInputEditText) {
            var start = view.selectionStart
            var end = view.selectionEnd
            if (start > end) {
                val temp = start
                start = end
                end = temp
            }
            if (text.isNotEmpty()) {
                val edit = view.editableText
                if (start < 0 || start >= edit.length) {
                    edit.append(text)
                } else {
                    edit.replace(start, end, text)
                }
            }
        } else {
            editor.insertText(text, text.length)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun onUndoClicked() {
        editor.undo()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun onRedoClicked() {
        editor.redo()
    }
}

class CodeEditComposeBinding(context: Context) : ViewBinding {

    val titleBar = TitleBar(context).apply {
        id = R.id.title_bar
        setTitle(R.string.edit_code)
    }

    val editText = CodeEditor(context).apply {
        id = R.id.editText
        setTextSize(18f)
    }

    val tvSearchResultLabel = TextView(context).apply {
        id = R.id.tv_search_result_label
        setText(R.string.search_result)
        textSize = 14f
    }

    val tvSearchResult = TextView(context).apply {
        id = R.id.tv_search_result
        text = "0"
        textSize = 14f
    }

    val switchRegex = Switch(context).apply {
        id = R.id.switch_regex
        isChecked = true
        setText(R.string.regex)
    }

    val tvFindLabel = TextView(context).apply {
        id = R.id.tv_find_label
        setText(R.string.find)
        setTextColor(context.getCompatColor(R.color.primaryText))
        textSize = 14f
    }

    val etFind = TextInputEditText(context).apply {
        id = R.id.et_find
    }

    val findTextInputLayout = TextInputLayout(context).apply {
        boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_NONE
        addView(etFind)
    }

    val btnCloseFind = ImageView(context).apply {
        id = R.id.btn_close_find
        setImageResource(R.drawable.ic_baseline_close)
        val p = 8.dpToPx()
        setPadding(p, p, p, p)
        contentDescription = context.getString(R.string.close)
    }

    val tvReplaceLabel = TextView(context).apply {
        id = R.id.tv_replace_label
        setText(R.string.replace)
        setTextColor(context.getCompatColor(R.color.primaryText))
        textSize = 14f
    }

    val etReplace = TextInputEditText(context).apply {
        id = R.id.et_replace
    }

    val replaceTextInputLayout = TextInputLayout(context).apply {
        boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_NONE
        addView(etReplace)
    }

    val btnCloseReplace = ImageView(context).apply {
        id = R.id.btn_close_replace
        setImageResource(R.drawable.ic_baseline_close)
        val p = 8.dpToPx()
        setPadding(p, p, p, p)
        contentDescription = context.getString(R.string.close)
    }

    val btnPrevious = Button(context, null, android.R.attr.buttonBarButtonStyle).apply {
        id = R.id.btn_previous
        setText(R.string.btn_previous)
        setTextColor(context.getCompatColor(R.color.primaryText))
        textSize = 14f
    }

    val btnNext = Button(context, null, android.R.attr.buttonBarButtonStyle).apply {
        id = R.id.btn_next
        setText(R.string.btn_next)
        setTextColor(context.getCompatColor(R.color.primaryText))
        textSize = 14f
    }

    val btnReplace = Button(context, null, android.R.attr.buttonBarButtonStyle).apply {
        id = R.id.btn_replace
        setText(R.string.replace)
        setTextColor(context.getCompatColor(R.color.primaryText))
        textSize = 14f
    }

    val btnReplaceAll = Button(context, null, android.R.attr.buttonBarButtonStyle).apply {
        id = R.id.btn_replace_all
        setText(R.string.replace_all)
        textSize = 14f
        isEnabled = false
    }

    val replaceGroup = LinearLayout(context).apply {
        id = R.id.replace_group
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        visibility = View.GONE
        addView(tvReplaceLabel, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))
        addView(replaceTextInputLayout, LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        ))
        addView(btnCloseReplace, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))
    }

    val searchGroup = LinearLayout(context).apply {
        id = R.id.search_group
        orientation = LinearLayout.VERTICAL
        val hPad = 12.dpToPx()
        setPadding(hPad, 0, hPad, 0)
        visibility = View.GONE
        setBackgroundColor(context.getCompatColor(R.color.background_card))

        addView(LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            addView(tvSearchResultLabel, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ))
            addView(tvSearchResult, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = 8.dpToPx()
            })
            addView(Space(context), LinearLayout.LayoutParams(
                0,
                0,
                1f
            ))
            addView(switchRegex, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ))
        }, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))

        addView(LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            clipChildren = false
            clipToPadding = false
            gravity = Gravity.CENTER_VERTICAL
            addView(tvFindLabel, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ))
            addView(findTextInputLayout, LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ))
            addView(btnCloseFind, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ))
        }, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))

        addView(replaceGroup, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))

        addView(LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(btnPrevious, LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ))
            addView(btnNext, LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ))
            addView(btnReplace, LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ))
            addView(btnReplaceAll, LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ))
        }, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))
    }

    val mainContainer = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        addView(titleBar, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))
        addView(editText, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1f
        ))
        addView(searchGroup, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))
    }

    val composeView = ComposeView(context)

    override fun getRoot(): View = composeView
}

@Composable
private fun CodeEditScreen(mainContainer: LinearLayout) {
    LegadoComposeTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            AndroidView(
                factory = { mainContainer },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
