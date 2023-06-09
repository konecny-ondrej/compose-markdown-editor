package me.okonecny.markdowneditor

interface LinkHandler {
    val linkAnnotationTag: String

    fun linkActivated(annotationValue: String)

    fun parseLinkAnnotation(url: String): String?
}