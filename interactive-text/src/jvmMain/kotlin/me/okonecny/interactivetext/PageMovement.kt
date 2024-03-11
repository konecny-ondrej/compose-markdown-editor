package me.okonecny.interactivetext

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*

internal fun Modifier.keyboardPageMovement(
    navigation: Navigation
) = onKeyEvent { keyEvent: KeyEvent ->
    if (keyEvent.type != KeyEventType.KeyDown) return@onKeyEvent false
    when (keyEvent.key) {
        Key.PageUp -> navigation.requestScroll(ScrollPageUp())
        Key.PageDown -> navigation.requestScroll(ScrollPageDown())
    }
    false
}