package me.okonecny.interactivetext

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.text.TextLayoutResult

val LocalSelectionStyle = compositionLocalOf { SelectionStyle() }

fun Modifier.paintSelection(
    interactiveScope: InteractiveScope,
    interactiveId: InteractiveId
) = composed {
    val selectionStyle = LocalSelectionStyle.current
    drawWithContent {
        val selection = interactiveScope.selection
        if (selection.isEmpty
            || !interactiveScope.isPlaced
            || !interactiveScope.hasComponent(interactiveId)
            || !interactiveScope.isComponentBetween(
                interactiveId,
                selection.start.componentId,
                selection.end.componentId
            )
        ) {
            drawContent()
            return@drawWithContent
        }

        val component = interactiveScope.getComponent(interactiveId)
        val textLayout = component.textLayoutResult
        val componentCoordinates = component.attachedLayoutCoordinates
        if (textLayout == null || componentCoordinates == null) {
            drawContent()
            return@drawWithContent
        }

        val selectionStart = if (selection.start.componentId == interactiveId) {
            selection.start.visualOffset
        } else {
            0
        }
        val text = textLayout.layoutInput.text
        val selectionEnd = if (selection.end.componentId == interactiveId) {
            selection.end.visualOffset.coerceAtMost(text.length)
        } else {
            text.length
        }

        val componentSelectionPath = textLayout.getFilledPathForRange(
            selectionStart,
            selectionEnd,
            0f
        )

        drawContent()
        drawPath(componentSelectionPath, selectionStyle.fillColor)
    }
}

private fun TextLayoutResult.getFilledPathForRange(start: Int, end: Int, growBy: Float = 1f): Path {
    require(start in 0..end && end <= layoutInput.text.length) {
        "Start($start) or End($end) is out of range [0..${layoutInput.text.length})," +
                " or start > end!"
    }
    if (start == end) return Path()

    var closedPath = Path()
    for (characterPos in start..<end) {
        val characterBox = Path()
        val lineNo = getLineForOffset(characterPos)
        val lineTop = getLineTop(lineNo)
        val lineBottom = getLineBottom(lineNo)
        val charBounds = getBoundingBox(characterPos)
        characterBox.addRect(
            Rect(
                left = charBounds.left - growBy,
                top = lineTop - growBy,
                right = charBounds.right + growBy,
                bottom = lineBottom + growBy
            )
        )
        closedPath = Path.combine(
            PathOperation.Union,
            closedPath,
            characterBox
        )
    }

    return closedPath
}