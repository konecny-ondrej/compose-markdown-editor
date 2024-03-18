package me.okonecny.markdowneditor

import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
class DocumentTheme(
    val styles: DocumentStyles = DocumentStyles(),
    val lineStyle: BorderStroke = BorderStroke(1.dp, Color.Black)
) {
    companion object {
        val default: DocumentTheme = DocumentTheme(DocumentStyles())
        internal val current: DocumentTheme
            @Composable
            @ReadOnlyComposable
            get() = LocalDocumentTheme.current
    }

    fun copy(
        styles: DocumentStyles = this.styles.copy(),
        lineStyle: BorderStroke = this.lineStyle.copy()
    ) = DocumentTheme(
        styles = styles,
        lineStyle = lineStyle
    )
}

internal val LocalDocumentTheme = compositionLocalOf { DocumentTheme.default }
