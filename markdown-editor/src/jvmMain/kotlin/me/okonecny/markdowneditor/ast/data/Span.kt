package me.okonecny.markdowneditor.ast.data

import me.okonecny.wysiwyg.ast.data.HasText

data object StrongEmphasis
data object Emphasis
data object Strikethrough
data object CodeSpan

data class Link(
    val target: String,
    val title: String?
)

data class AutoLink(
    val target: String
) : HasText {
    override val text: String by ::target
}

data class Anchor(val name: String)

data class Space(val count: UInt)

data class Image(
    val url: String,
    val title: String?
)

data object SoftLineBreak
data object HardLineBreak
data object TextBase
data class UserMention(
    val username: String
) : HasText {
    override val text: String by ::username
}

data object HtmlEntity

data class Emoji(
    val shortcut: String,
    val unicode: String
)