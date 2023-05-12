package me.okonecny.markdowneditor.inline

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.sp
import com.vladsch.flexmark.ast.Image
import me.okonecny.interactivetext.ConstantTextMapping
import me.okonecny.markdowneditor.MappedText
import me.okonecny.markdowneditor.ZERO_WIDTH_SPACE

private const val IMAGE_INLINE_ELEMENT_TYPE = "me.okonecny.markdowneditor.inline.Image"

@Composable
internal fun MappedText.Builder.appendImage(
    image: Image
) {
    append(ZERO_WIDTH_SPACE) // So we don't have an empty paragraph.
    val imageUrl = image.url.toString()
    // TODO: load image
    // TODO: image cache so we don't load the image multiple times
    val placeholder = Placeholder(30.sp, 30.sp, PlaceholderVerticalAlign.AboveBaseline)

    appendInlineContent(
        image.altText(visualLength),
        IMAGE_INLINE_ELEMENT_TYPE
    ) {
        InlineTextContent(placeholder) {
            Image(image)
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
private fun Image(image: Image) {
    var loadState by remember { mutableStateOf(ImageLoadState.initial) }

    with(LocalDensity.current) {
        when (loadState) {
            ImageLoadState.UNLOADED -> {
                Box(
                    modifier = Modifier.size(30.sp.toDp()).border(1.sp.toDp(), Color.Red)
                        .clickable(onClick = { loadState = ImageLoadState.LOADED_SUCCESS })
                )
            }

            ImageLoadState.LOADING -> TODO()
            ImageLoadState.LOADED_SUCCESS -> {
                Box(modifier = Modifier.size(30.sp.toDp()).border(1.sp.toDp(), Color.Cyan))

            }

            ImageLoadState.LOADED_FAILURE -> TODO()
        }
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