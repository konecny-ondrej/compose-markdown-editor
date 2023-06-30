package me.okonecny.markdowneditor

interface LinkHandler {
    val linkAnnotationTag: String get() = this::class.qualifiedName!!

    fun linkActivated(annotationValue: String)

    fun parseLinkAnnotation(url: String): String?
}