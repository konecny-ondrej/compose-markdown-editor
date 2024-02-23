package me.okonecny.interactivetext

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val LocalInteractiveScope = compositionLocalOf<InteractiveScope?> { null }
val LocalInteractiveInputHandler = compositionLocalOf<(TextInputCommand) -> Unit> { {} }

/**
 * Delimits region where all the interactive components will be considered parts of the same document.
 * @param scope State of this interactive region. Pass null to disable interaction.
 * @param interactiveContent Child components.
 */
@Composable
fun InteractiveContainer(
    scope: InteractiveScope? = rememberInteractiveScope(),
    selectionStyle: SelectionStyle = SelectionStyle(),
    modifier: Modifier = Modifier,
    onInput: (TextInputCommand) -> Unit = {},
    onCursorMovement: (CursorPosition) -> Unit = { scope?.cursorPosition = it },
    interactiveContent: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalInteractiveScope provides scope,
        LocalInteractiveInputHandler provides onInput,
        LocalSelectionStyle provides selectionStyle
    ) {
        val interactiveModifier = if (scope == null) {
            Modifier
        } else {
            val requester = scope.focusRequester
            Modifier
                .focusRequester(requester)
                .focusable()
                .onGloballyPositioned { layoutCoordinates ->
                    scope.place(layoutCoordinates)
                }
                .keyboardCursorMovement(scope) { newCursorPosition, newSelection ->
                    scope.selection = newSelection
                    onCursorMovement(newCursorPosition)
                }
                .pointerCursorMovement(scope) { newCursorPosition, newSelection ->
                    requester.requestFocus()
                    scope.selection = newSelection
                    onCursorMovement(newCursorPosition)
                }
                .onKeyEvent { keyEvent: KeyEvent ->
                    if (keyEvent.key == Key.Escape) {
                        scope.selection = Selection.empty
                    }
                    false
                }
                .textInput(onInput = onInput)
        }
        Box(modifier = interactiveModifier.then(modifier)) {
            interactiveContent()
        }
    }
}

@Composable
fun rememberInteractiveScope(vararg keys: Any?) = remember(keys) { InteractiveScope() }

/**
 * Delimits a region where interaction is disabled.
 */
@Composable
fun DisabledInteractiveContainer(interactiveContent: @Composable () -> Unit) =
    InteractiveContainer(scope = null, interactiveContent = interactiveContent)

data class SelectionStyle(
    val fillColor: Color = Color.Cyan.copy(alpha = 0.5f),
    val stroke: Stroke = Stroke()
) {
    data class Stroke(
        val width: Dp = 2.dp,
        val color: Color = Color.Cyan
    )
}
