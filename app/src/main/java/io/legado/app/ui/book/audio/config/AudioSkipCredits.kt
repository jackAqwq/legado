package io.legado.app.ui.book.audio.config

import android.os.Bundle
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.legado.app.R
import io.legado.app.base.ComposeDialogFragment
import io.legado.app.data.entities.Book
import io.legado.app.ui.compose.LegadoComposeTheme
import io.legado.app.ui.compose.LegadoDialogSectionTitle
import io.legado.app.ui.compose.LegadoSliderRow
import java.lang.ref.WeakReference
import android.content.DialogInterface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import io.legado.app.utils.setLayout

class AudioSkipCredits : ComposeDialogFragment() {
    private var openCredits by mutableIntStateOf(0)
    private var closeCredits by mutableIntStateOf(0)

    companion object {
        private var bookRef: WeakReference<Book>? = null

        fun newInstance(book: Book): AudioSkipCredits {
            return AudioSkipCredits().apply {
                bookRef = WeakReference(book)
            }
        }
    }

    private val book: Book by lazy {
        bookRef?.get() ?: throw IllegalStateException("Book reference lost")
    }

    override fun onStart() {
        super.onStart()
        setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onDialogCreated(savedInstanceState: Bundle?) {
        openCredits = book.getOpenCredits()
        closeCredits = book.getCloseCredits()
    }

    @Composable
    override fun DialogContent() {
        AudioSkipCreditsContent(
            openCredits = openCredits,
            closeCredits = closeCredits,
            onOpenCreditsChange = {
                openCredits = it
                book.setOpenCredits(it)
            },
            onCloseCreditsChange = {
                closeCredits = it
                book.setCloseCredits(it)
            },
        )
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        book.save()
    }
}

@Composable
private fun AudioSkipCreditsContent(
    openCredits: Int,
    closeCredits: Int,
    onOpenCreditsChange: (Int) -> Unit,
    onCloseCreditsChange: (Int) -> Unit,
) {
    LegadoComposeTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                LegadoDialogSectionTitle(text = stringResource(R.string.skip_book_credits))
                LegadoSliderRow(
                    title = stringResource(R.string.audio_opening_credits),
                    value = openCredits,
                    valueRange = 0..180,
                    valueText = { "${it}s" },
                    onValueChange = onOpenCreditsChange,
                )
                LegadoSliderRow(
                    title = stringResource(R.string.audio_ending_credits),
                    value = closeCredits,
                    valueRange = 0..180,
                    valueText = { "${it}s" },
                    onValueChange = onCloseCreditsChange,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AudioSkipCreditsContentPreview() {
    AudioSkipCreditsContent(
        openCredits = 15,
        closeCredits = 20,
        onOpenCreditsChange = {},
        onCloseCreditsChange = {},
    )
}
