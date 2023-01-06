package me.okonecny.markdowneditor

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    val code: TextStyle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontFamily = FontFamily.Monospace,
        fontSize = 20.sp,
        letterSpacing = 0.5.sp
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
        code: TextStyle = this.code
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
        code = code
    )
}