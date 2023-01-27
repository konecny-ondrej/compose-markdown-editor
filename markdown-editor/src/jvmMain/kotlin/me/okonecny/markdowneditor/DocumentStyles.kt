package me.okonecny.markdowneditor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
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
class DocumentStyles(
    val defaultFontFamily: FontFamily = FontFamily.Default,
    val h1: TextStyle = TextStyle(
        fontWeight = FontWeight.Light,
        fontSize = 96.sp,
        letterSpacing = (-1.5).sp
    ),
    val h2: TextStyle = TextStyle(
        fontWeight = FontWeight.Light,
        fontSize = 60.sp,
        letterSpacing = (-0.5).sp
    ),
    val h3: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 48.sp,
        letterSpacing = 0.sp
    ),
    val h4: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 34.sp,
        letterSpacing = 0.25.sp
    ),
    val h5: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        letterSpacing = 0.sp
    ),
    val h6: TextStyle = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        letterSpacing = 0.15.sp
    ),
    val paragraph: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        letterSpacing = 0.5.sp,
        textAlign = TextAlign.Justify
    ),
    val listNumber: TextStyle = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        letterSpacing = 0.5.sp,
        textAlign = TextAlign.Left
    ),
    val inlineCode: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontFamily = FontFamily.Monospace,
        fontSize = 20.sp,
        letterSpacing = 0.5.sp,
        background = Color.LightGray
    ),
    val codeBlock: BlockStyle = BlockStyle(
        textStyle = inlineCode.copy(background = Color.Transparent),
        modifier = Modifier
            .padding(15.dp)
//            .shadow(10.dp, RoundedCornerShape(5.dp), spotColor = Color.DarkGray)
            .border(Dp.Hairline, lerp(Color.LightGray, Color.Black, 0.1f), RoundedCornerShape(5.dp))
            .background(Color.LightGray, RoundedCornerShape(5.dp))
            .padding(10.dp, 5.dp)
    ),
    val blockQuote: BoxStyle = BoxStyle(
        modifier = Modifier
            .padding(15.dp)
            .border(Dp.Hairline, lerp(Color.LightGray, Color.Black, 0.1f), RoundedCornerShape(5.dp))
            .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(5.dp))
            .clip(RoundedCornerShape(5.dp))
            .leftBorder(5.dp, lerp(Color.LightGray, Color.Black, 0.2f))
            .padding(10.dp, 5.dp)

    ),
    val link: TextStyle = paragraph.copy(
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
    )
) {
    fun copy(
        defaultFontFamily: FontFamily = this.defaultFontFamily,
        h1: TextStyle = this.h1,
        h2: TextStyle = this.h2,
        h3: TextStyle = this.h3,
        h4: TextStyle = this.h4,
        h5: TextStyle = this.h5,
        h6: TextStyle = this.h6,
        paragraph: TextStyle = this.paragraph,
        listNumber: TextStyle = this.listNumber,
        inlineCode: TextStyle = this.inlineCode,
        codeBlock: BlockStyle = this.codeBlock,
        blockQuote: BoxStyle = this.blockQuote,
        link: TextStyle = this.link,
        emphasis: TextStyle = this.emphasis,
        strong: TextStyle = this.strong,
        strikethrough: TextStyle = this.strikethrough
    ) = DocumentStyles(
        defaultFontFamily = defaultFontFamily,
        h1 = h1,
        h2 = h2,
        h3 = h3,
        h4 = h4,
        h5 = h5,
        h6 = h6,
        paragraph = paragraph,
        listNumber = listNumber,
        inlineCode = inlineCode,
        codeBlock = codeBlock,
        blockQuote = blockQuote,
        link = link,
        emphasis = emphasis,
        strong = strong,
        strikethrough = strikethrough
    )
}

data class BoxStyle(
    val modifier: Modifier
)

data class BlockStyle(
    val textStyle: TextStyle,
    val modifier: Modifier
)