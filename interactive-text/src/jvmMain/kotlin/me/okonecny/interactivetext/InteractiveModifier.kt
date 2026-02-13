package me.okonecny.interactivetext

import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import me.okonecny.wysiwyg.ast.VisualNode

fun <T : Any, D : Any> Modifier.interactive(
    node: VisualNode<T, D>
) = interactiveText(
    textLayoutResult = null,
    textLength = 0,
    node = node
)

fun <T : Any, D : Any> Modifier.interactiveText(
    textLayoutResult: TextLayoutResult?,
    textLength: Int,
    userData: UserData = UserData.empty,
    node: VisualNode<T, D>
) = composed {
    val interactiveScope = LocalInteractiveScope.current ?: return@composed Modifier
    val cursorPosition = interactiveScope.cursorPosition
    val scrollIndex = LocalScrollIndex.current

    var interactiveModifier: Modifier = Modifier
    if (cursorPosition != null && cursorPosition.componentId == node.interactiveId) {
        interactiveModifier = interactiveModifier
            .cursorLine(
                textLayoutResult,
                cursorPosition.visualOffset
            )
    }

    interactiveModifier = interactiveModifier
        .onGloballyPositioned { layoutCoordinates ->
            interactiveScope.register(
                InteractiveComponent(
                    id = node.interactiveId,
                    scrollIndex = scrollIndex,
                    layoutCoordinates = layoutCoordinates,
                    visualTextRange = TextRange(0, textLength),
                    textLayoutResult = textLayoutResult,
                    userData = userData
                )
            )
        }
        .paintComponentSelection(
            interactiveScope,
            node
        )
    DisposableEffect(interactiveScope) {
        onDispose {
            interactiveScope.unregister(node.interactiveId)
        }
    }
    return@composed interactiveModifier
}