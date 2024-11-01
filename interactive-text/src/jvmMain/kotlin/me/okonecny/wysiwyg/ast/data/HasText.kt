package me.okonecny.wysiwyg.ast.data

interface HasText<T : HasText<T>> {
    val text: String

    fun replaceText(text: String): T
}