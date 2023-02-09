package me.okonecny.markdowneditor.internal.interactive

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle

@Composable
internal fun InteractiveText(
    text: AnnotatedString,
    style: TextStyle = LocalTextStyle.current,
    modifier: Modifier = Modifier,
    inlineContent: Map<String, InlineTextContent> = mapOf()
) {
    // TODO: cursor
    // TODO: selection
    // TODO: clickable links
    Text( // TODO: use BasicText?
        text = text,
        style = style,
        modifier = modifier,
        inlineContent = inlineContent
    )
}

@Composable
internal fun InteractiveText(
    text: String,
    style: TextStyle = LocalTextStyle.current,
    modifier: Modifier = Modifier
) = InteractiveText(AnnotatedString(text), style, modifier)