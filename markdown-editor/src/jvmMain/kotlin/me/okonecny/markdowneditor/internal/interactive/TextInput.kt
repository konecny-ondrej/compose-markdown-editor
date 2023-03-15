package me.okonecny.markdowneditor.internal.interactive

import androidx.compose.foundation.text.isTypedEvent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeOptions
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TextInputSession

internal fun Modifier.textInput (
    enabled: Boolean
): Modifier = composed {
    var textInputSession by remember { mutableStateOf<TextInputSession?>(null) }
    val textInputService = LocalTextInputService.current

    if (textInputSession == null && textInputService != null && enabled) {
        System.err.println("start input")
        textInputSession = textInputService.startInput(
            value = TextFieldValue(AnnotatedString(""), TextRange(0,1)),
            imeOptions = ImeOptions.Default,
            onEditCommand = { editCommands ->
                System.err.println(editCommands)
            },
            onImeActionPerformed = { imeAction ->
                System.err.println(imeAction)
            }
        )
    }
    val session = textInputSession
    if (session != null && !enabled && textInputService != null) {
        System.err.println("stop input")
        textInputService.stopInput(session)
        textInputSession = null
    }

    return@composed onKeyEvent { keyEvent: KeyEvent ->
        if (keyEvent.isTypedEvent) {
            val letter = StringBuilder().appendCodePoint(keyEvent.utf16CodePoint).toString() // TODO decode UTF-16 properly
            System.err.println(letter)
        }
        false
    }
}