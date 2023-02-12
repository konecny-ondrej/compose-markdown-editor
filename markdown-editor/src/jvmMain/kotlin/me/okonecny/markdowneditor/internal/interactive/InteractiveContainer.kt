package me.okonecny.markdowneditor.internal.interactive

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onGloballyPositioned

internal val LocalInteractiveScope = compositionLocalOf<InteractiveScope?> { null }

/**
 * Delimits region where all the interactive components will be considered parts of the same document.
 * @param scope State of this interactive region. Pass null to disable interaction.
 * @param interactiveContent Child components.
 */
@Composable
fun InteractiveContainer(
    scope: InteractiveScope? = rememberInteractiveScope(),
    interactiveContent: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalInteractiveScope provides scope
    ) {
        val interactiveModifier = if (scope == null) {
            Modifier
        } else {
            val requester = remember { FocusRequester() }
            Modifier
                .focusRequester(requester)
                .focusable()
                .onGloballyPositioned { layoutCoordinates ->
                    scope.containerCoordinates = layoutCoordinates
                }
                .cursorKeyboardInteraction(scope) { newCursorPosition ->
                    scope.cursorPosition.value = newCursorPosition
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { // TODO: remove the effect on click.
                    requester.requestFocus()
                }
        }
        Box(modifier = interactiveModifier) {
            interactiveContent()
        }
    }
}

@Composable
fun rememberInteractiveScope(vararg keys: Any?) = remember(keys) { InteractiveScope() }