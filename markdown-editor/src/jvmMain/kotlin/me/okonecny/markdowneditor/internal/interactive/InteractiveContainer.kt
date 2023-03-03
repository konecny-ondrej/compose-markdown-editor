package me.okonecny.markdowneditor.internal.interactive

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle

private val defaultSelectionStyle = TextStyle(color = Color.Cyan.copy(alpha = 0.5f))
internal val LocalInteractiveScope = compositionLocalOf<InteractiveScope?> { null }
internal val LocalSelectionStyle = compositionLocalOf { defaultSelectionStyle }

/**
 * Delimits region where all the interactive components will be considered parts of the same document.
 * @param scope State of this interactive region. Pass null to disable interaction.
 * @param interactiveContent Child components.
 */
@Composable
fun InteractiveContainer(
    scope: InteractiveScope? = rememberInteractiveScope(),
    selectionStyle: TextStyle = defaultSelectionStyle,
    interactiveContent: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalInteractiveScope provides scope,
        LocalSelectionStyle provides selectionStyle
    ) {
        val interactiveModifier = if (scope == null) {
            Modifier
        } else {
            val requester = remember { FocusRequester() }
            Modifier
                .focusRequester(requester)
                .focusable()
                .onGloballyPositioned { layoutCoordinates ->
                    scope.place(layoutCoordinates)
                }
                .keyboardCursorMovement(scope) { newCursorPosition ->
                    scope.cursorPosition.value = newCursorPosition
                }
                .pointerCursorMovement(scope) { newCursorPosition ->
                    requester.requestFocus()
                    scope.cursorPosition.value = newCursorPosition
                }
        }
        Box(modifier = interactiveModifier) {
            interactiveContent()
        }
    }
}

@Composable
fun rememberInteractiveScope(vararg keys: Any?) = remember(keys) { InteractiveScope() }