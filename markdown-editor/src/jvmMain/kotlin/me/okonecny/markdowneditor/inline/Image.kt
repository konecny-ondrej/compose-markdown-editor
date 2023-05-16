package me.okonecny.markdowneditor.inline

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vladsch.flexmark.ast.Image
import me.okonecny.interactivetext.ConstantTextMapping
import me.okonecny.markdowneditor.MappedText
import me.okonecny.markdowneditor.ZERO_WIDTH_SPACE
import java.util.concurrent.atomic.AtomicLong

private const val IMAGE_INLINE_ELEMENT_TYPE = "me.okonecny.markdowneditor.inline.Image"

private val imageCount = AtomicLong(0L) // TODO: store this in the mapped text builder?

@Composable
internal fun MappedText.Builder.appendImage(
    image: Image,
    imageSize: IntSize,
    onSizeChange: (IntSize) -> Unit
) {
    append(ZERO_WIDTH_SPACE) // So we don't have an empty paragraph.
    val imageUrl = image.url.toString()
    // TODO: load image
    // TODO: image cache so we don't load the image multiple times

    // Consider the image size to be in DP so images still occupy the same space visually in the document,
    // at the expense of potentially reduced quality.
    val convertedImageSize = DpSize(imageSize.width.dp, imageSize.height.dp)
    val placeholder = with(LocalDensity.current) {
        Placeholder(
            convertedImageSize.width.toSp(),
            convertedImageSize.height.toSp(),
            PlaceholderVerticalAlign.AboveBaseline
        )
    }

    appendInlineContent(
        image.altText(visualLength),
        IMAGE_INLINE_ELEMENT_TYPE + imageCount.getAndIncrement()
    ) {
        InlineTextContent(placeholder) {
            Image(image, convertedImageSize, onSizeChange)
        }
    }
}


internal enum class ImageLoadState {
    UNLOADED,
    LOADING,
    LOADED_SUCCESS,
    LOADED_FAILURE;

    companion object {
        val initial = UNLOADED
    }
}

@Composable
private fun Image(image: Image, imageSize: DpSize, onSizeChange: (IntSize) -> Unit) {
    var loadState by remember { mutableStateOf(ImageLoadState.initial) }

    when (loadState) {
        ImageLoadState.UNLOADED -> {
            Box(
                modifier = Modifier
                    .size(imageSize.width, imageSize.height)
                    .border(with(LocalDensity.current) { 1.sp.toDp() }, Color.Red)
                    .clickable(onClick = {
                        onSizeChange(IntSize(40, 40))
                        loadState = ImageLoadState.LOADED_SUCCESS
                    })
            ) {
                Text(text = image.url.toString())
            }
        }

        ImageLoadState.LOADING -> TODO()
        ImageLoadState.LOADED_SUCCESS -> {
            Box(
                modifier = Modifier
                    .size(imageSize.width, imageSize.height)
                    .border(with(LocalDensity.current) { 1.sp.toDp() }, Color.Cyan)
            )
        }

        ImageLoadState.LOADED_FAILURE -> TODO()
    }
}

private fun Image.altText(visualOffset: Int): MappedText = MappedText(
    text = " ",
    textMapping = ConstantTextMapping(
        coveredSourceRange = TextRange(startOffset, endOffset),
        visualTextRange = TextRange(visualOffset, visualOffset + 1)
    )
)

@Composable
internal fun LoadingIndicator() {
    // TODO
}