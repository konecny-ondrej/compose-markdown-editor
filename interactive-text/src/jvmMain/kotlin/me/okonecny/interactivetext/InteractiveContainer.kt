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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.ceil
import kotlin.math.floor

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
    val navigation = remember(scope) { ScrollableNavigation() }
    CompositionLocalProvider(
        LocalInteractiveScope provides scope,
        LocalInteractiveInputHandler provides onInput,
        LocalSelectionStyle provides selectionStyle,
        LocalNavigation provides navigation
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
                .keyboardCursorMovement(scope, navigation) { newCursorPosition, newSelection ->
                    scope.selection = newSelection
                    val newComponentUnderCursor = scope.getComponent(newCursorPosition.componentId)
                    if (newComponentUnderCursor.isLaidOut) {
                        onCursorMovement(newCursorPosition)
                    } else {
                        navigation.requestScroll(ScrollToComponent(newComponentUnderCursor))
                    }
                }
                .keyboardPageMovement(navigation)
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
                .textInput(onInput = { textInputCommand ->
                    if (scope.isCursorVisible) {
                        onInput(textInputCommand)
                    } else {
                        val componentUnderCursor = scope.componentUnderCursor
                        if (componentUnderCursor != null) navigation.requestScroll(
                            ScrollToComponent(componentUnderCursor)
                        )
                    }
                })
        }
        Box(modifier = interactiveModifier.then(modifier)) {
            interactiveContent()
        }
        LaunchedEffect(scope?.cursorPosition) {
            navigation.ensureCursorIsVisible(scope)
        }
    }
}

fun Navigation.ensureCursorIsVisible(scope: InteractiveScope?) {
    if (scope == null) return
    val cursorVisualRect = scope.cursorVisualRect(scope.cursorPosition ?: return) ?: return
    requestScroll(
        ScrollToMakeVisible(
            IntRange(
                floor(cursorVisualRect.top).toInt(),
                ceil(cursorVisualRect.bottom).toInt()
            )
        )
    )
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
