package me.okonecny.interactivetext

import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange

fun Modifier.interactiveText(
    interactiveId: InteractiveId,
    textLayoutResult: TextLayoutResult?,
    textMapping: TextMapping,
    textLength: Int,
    userData: UserData = UserData.empty
) = composed {
    val interactiveScope = LocalInteractiveScope.current ?: return@composed Modifier
    val cursorPosition = interactiveScope.cursorPosition
    val scrollIndex = LocalScrollIndex.current

    var interactiveModifier: Modifier = Modifier
    if (cursorPosition != null && cursorPosition.componentId == interactiveId) {
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
                    id = interactiveId,
                    scrollIndex = scrollIndex,
                    layoutCoordinates = layoutCoordinates,
                    visualTextRange = TextRange(0, textLength),
                    textMapping = textMapping,
                    textLayoutResult = textLayoutResult,
                    userData = userData
                )
            )
        }
        .paintSelection(
            interactiveScope,
            interactiveId
        )
    DisposableEffect(interactiveScope) {
        onDispose {
            interactiveScope.register(
                InteractiveComponent(
                    id = interactiveId,
                    scrollIndex = scrollIndex,
                    layoutCoordinates = null,
                    visualTextRange = TextRange(0, 0),
                    textMapping = ZeroTextMapping,
                    textLayoutResult = null,
                    userData = userData
                )
            )
        }
    }
    return@composed interactiveModifier
}