package me.okonecny.interactivetext

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import me.okonecny.wysiwyg.ast.VisualNode

val LocalSelectionStyle = compositionLocalOf { SelectionStyle() }

fun <T : Any, D : Any> Modifier.paintComponentSelection(
    interactiveScope: InteractiveScope,
    node: VisualNode<T, D>
) = composed {
    val selectionStyle = LocalSelectionStyle.current
    drawWithContent {
        val selection = interactiveScope.selection
        if (selection.isEmpty
            || !interactiveScope.isPlaced
            || !interactiveScope.hasComponent(node.interactiveId)
            || !node.isBetweenInReadingOrder(
                node.root.findChildById(selection.start.componentId)!!,
                node.root.findChildById(selection.end.componentId)!!
            )
        ) {
            drawContent()
            return@drawWithContent
        }

        val component = interactiveScope.getComponent(node.interactiveId)
        val textLayout = component.textLayoutResult
        val componentCoordinates = component.attachedLayoutCoordinates
        if (textLayout == null || componentCoordinates == null) {
            drawContent()
            return@drawWithContent
        }

        val selectionStart = if (selection.start.componentId == node.interactiveId) {
            selection.start.visualOffset
        } else {
            0
        }
        val text = textLayout.layoutInput.text
        val selectionEnd = if (selection.end.componentId == node.interactiveId) {
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

internal fun <D : Any> Modifier.paintContainerSelection(
    interactiveScope: InteractiveScope,
    selectionStyle: SelectionStyle,
    document: VisualNode<D, D>
) = clip(RectangleShape)
    .drawWithContent {
        val selection = interactiveScope.selection
        if (selection.isEmpty
            || !interactiveScope.isPlaced
//            || !interactiveScope.hasComponent(selection.start.componentId)
//            || !interactiveScope.hasComponent(selection.end.componentId)
        ) {
            drawContent()
            return@drawWithContent
        }

        var combinedSelectionPath = Path()
        for (node in document
            .findChildById(selection.start.componentId)
            ?.findAllSuccessorsWhile<Any>(VisualNode<Any, D>::nextNodeInReadingOrder) {
                it.interactiveId != selection.end.componentId
            } ?: listOf()
        ) {
            if (!interactiveScope.hasComponent(node.interactiveId)) continue
            val component = interactiveScope.getComponent(node.interactiveId)
            val textLayout = component.textLayoutResult ?: continue
            val componentCoordinates = component.attachedLayoutCoordinates ?: continue

            val selectionStart = if (selection.start.componentId == component.id) {
                selection.start.visualOffset
            } else {
                0
            }
            val text = textLayout.layoutInput.text
            val selectionEnd = if (selection.end.componentId == component.id) {
                selection.end.visualOffset.coerceAtMost(text.length)
            } else {
                text.length
            }

            val componentSelectionPath = textLayout.getFilledPathForRange(
                selectionStart,
                selectionEnd,
                (selectionStyle.stroke.width + 1.dp).toPx()
            )
            val positionInContainer = interactiveScope
                .containerCoordinates
                .localPositionOf(componentCoordinates, Offset.Zero)
            componentSelectionPath.translate(positionInContainer)
            combinedSelectionPath = Path.combine(
                PathOperation.Union,
                combinedSelectionPath,
                componentSelectionPath
            )
        }

        drawContent()
        drawPath(combinedSelectionPath, selectionStyle.fillColor)
        drawPath(
            combinedSelectionPath,
            selectionStyle.stroke.color,
            style = Stroke(
                width = selectionStyle.stroke.width.toPx(),
                join = StrokeJoin.Round
            )
        )
    }