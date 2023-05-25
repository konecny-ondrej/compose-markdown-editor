package me.okonecny.markdowneditor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Immutable
data class DocumentStyles(
    val defaultFontFamily: FontFamily = FontFamily.Default,
    val blockShape: Shape = RoundedCornerShape(5.dp),
    val paragraph: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        letterSpacing = 0.5.sp,
        textAlign = TextAlign.Justify,
        lineHeight = 14.sp
    ),
    val h1: TextStyle = TextStyle(
        fontWeight = FontWeight.Light,
        fontSize = paragraph.fontSize * 3.5,
        letterSpacing = (-1.5).sp
    ),
    val h2: TextStyle = TextStyle(
        fontWeight = FontWeight.Light,
        fontSize = paragraph.fontSize * 3,
        letterSpacing = (-0.5).sp
    ),
    val h3: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = paragraph.fontSize * 2.5,
        letterSpacing = 0.sp
    ),
    val h4: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = paragraph.fontSize * 2,
        letterSpacing = 0.25.sp
    ),
    val h5: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = paragraph.fontSize * 1.7,
        letterSpacing = 0.sp
    ),
    val h6: TextStyle = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = paragraph.fontSize * 1.2,
        letterSpacing = 0.15.sp
    ),
    val listNumber: TextStyle = TextStyle(
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Left
    ),
    val taskListCheckbox: BoxStyle = BoxStyle(
        modifier = Modifier.composed {
            padding(2.dp, 0.dp, 4.dp, 0.dp)
                .scale(0.8f)
                .pointerHoverIcon(PointerIcon.Hand)
                .size(with(LocalDensity.current) {
                    paragraph.lineHeight.toDp()
                })

        }
    ),
    val inlineCode: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontFamily = FontFamily.Monospace,
        background = Color.LightGray
    ),
    val codeBlock: BlockStyle = BlockStyle(
        textStyle = inlineCode.copy(background = Color.Transparent),
        modifier = Modifier
            .padding(15.dp)
            .border(Dp.Hairline, lerp(Color.LightGray, Color.Black, 0.1f), blockShape)
            .background(Color.LightGray, blockShape)
            .padding(10.dp, 5.dp)
    ),
    val blockQuote: BoxStyle = BoxStyle(
        modifier = Modifier
            .padding(15.dp)
            .border(Dp.Hairline, lerp(Color.LightGray, Color.Black, 0.1f), blockShape)
            .background(Color.Black.copy(alpha = 0.2f), blockShape)
            .clip(blockShape)
            .leftBorder(5.dp, lerp(Color.LightGray, Color.Black, 0.2f))
            .padding(10.dp, 5.dp)

    ),
    val table: TableStyle = TableStyle(
        headerCellStyle = BlockStyle(
            textStyle = paragraph,
            modifier = Modifier
                .background(Color.LightGray)
                .leftBorder(Dp.Hairline, Color.Gray)
                .padding(8.dp)
        ),
        bodyCellStyle = BlockStyle(
            textStyle = paragraph,
            modifier = Modifier
                .leftBorder(Dp.Hairline, Color.LightGray)
                .topBorder(Dp.Hairline, Color.LightGray)
                .padding(8.dp)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(blockShape)
            .border(Dp.Hairline, lerp(Color.LightGray, Color.Black, 0.1f), blockShape)
    ),
    val link: TextStyle = TextStyle(
        color = Color.Blue,
        textDecoration = TextDecoration.Underline
    ),
    val emphasis: TextStyle = TextStyle(
        fontStyle = FontStyle.Italic
    ),
    val strong: TextStyle = TextStyle(
        fontWeight = FontWeight.Bold
    ),
    val strikethrough: TextStyle = TextStyle(
        textDecoration = TextDecoration.LineThrough
    ),
    val selection: TextStyle = TextStyle(
        background = Color.Cyan.copy(alpha = 0.5f)
    ),
    val image: ImageStyle = ImageStyle(
        modifier = Modifier
            .clip(blockShape)
            .border(Dp.Hairline, lerp(Color.LightGray, Color.Black, 0.1f), blockShape),
        title = BlockStyle(
            textStyle = paragraph.copy(textAlign = TextAlign.Center),
            modifier = Modifier
                .fillMaxWidth(1.0f)
                .background(Color.White.copy(alpha = 0.5f))
                .padding(4.dp)
        )
    )
)

data class ImageStyle(
    val modifier: Modifier,
    val title: BlockStyle
)

data class BoxStyle(
    val modifier: Modifier
)

data class BlockStyle(
    val textStyle: TextStyle,
    val modifier: Modifier
)

data class TableStyle(
    val headerCellStyle: BlockStyle,
    val bodyCellStyle: BlockStyle,
    val modifier: Modifier
)