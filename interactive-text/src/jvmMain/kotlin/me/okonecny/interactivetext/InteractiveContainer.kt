package me.okonecny.interactivetext

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle

private val defaultSelectionStyle = TextStyle(color = Color.Cyan.copy(alpha = 0.5f))
val LocalInteractiveScope = compositionLocalOf<InteractiveScope?> { null }
internal val LocalSelectionStyle = compositionLocalOf { defaultSelectionStyle }
val LocalInteractiveInputHandler = compositionLocalOf<(TextInputCommand) -> Unit> { {} }

/**
 * Delimits region where all the interactive components will be considered parts of the same document.
 * @param scope State of this interactive region. Pass null to disable interaction.
 * @param interactiveContent Child components.
 */
@Composable
fun InteractiveContainer(
    scope: InteractiveScope? = rememberInteractiveScope(),
    selectionStyle: TextStyle = defaultSelectionStyle,
    modifier: Modifier = Modifier,
    onInput: (TextInputCommand) -> Unit = {},
    onCursorMovement: (CursorPosition) -> Unit = { scope?.cursorPosition?.value = it },
    interactiveContent: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalInteractiveScope provides scope,
        LocalSelectionStyle provides selectionStyle,
        LocalInteractiveInputHandler provides onInput
    ) {
        val interactiveModifier = if (scope == null) {
            Modifier
        } else {
            val requester = scope.focusRequester
            var selection by scope.selection
            Modifier
                .focusRequester(requester)
                .focusable()
                .onGloballyPositioned { layoutCoordinates ->
                    scope.place(layoutCoordinates)
                }
                .keyboardCursorMovement(scope) { newCursorPosition, newSelection ->
                    selection = newSelection
                    onCursorMovement(newCursorPosition)
                }
                .pointerCursorMovement(scope) { newCursorPosition, newSelection ->
                    requester.requestFocus()
                    selection = newSelection
                    onCursorMovement(newCursorPosition)
                }
                .onKeyEvent { keyEvent: KeyEvent ->
                    if (keyEvent.key == Key.Escape) {
                        selection = Selection.empty
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