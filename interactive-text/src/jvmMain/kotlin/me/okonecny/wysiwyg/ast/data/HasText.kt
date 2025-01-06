package me.okonecny.wysiwyg.ast.data

interface HasText {
    val text: String

    fun replaceText(text: String): HasText
}