package me.okonecny.markdowneditor.ast.data

import me.okonecny.wysiwyg.ast.data.HasText
import me.okonecny.wysiwyg.ast.data.Text

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
    override fun replaceText(text: String): AutoLink = AutoLink(text)
}

data class Anchor(val name: String) : LinkTarget {
    override val anchorName: String by ::name
}

data class Space(val count: Int) : HasText {
    override val text: String = " ".repeat(count)
    override fun replaceText(text: String): Space = Space(text.length)
}

data class Image(
    val url: String,
    val title: String?
)

data object SoftLineBreak : HasText {
    override val text: String = "\n"
    override fun replaceText(text: String): HasText = Text(text)
}

data object HardLineBreak : HasText {
    override val text: String = "\n"
    override fun replaceText(text: String): HasText = Text(text)
}

data object TextBase
data class UserMention(
    val username: String
) : HasText {
    override val text: String by ::username
    override fun replaceText(text: String): UserMention = UserMention(text)
}

data object HtmlEntity

data class Emoji(
    val shortcut: String,
    val unicode: String
) : HasText {
    override val text: String = " ".repeat(unicode.length)
    override fun replaceText(text: String): HasText = Text(text)
}