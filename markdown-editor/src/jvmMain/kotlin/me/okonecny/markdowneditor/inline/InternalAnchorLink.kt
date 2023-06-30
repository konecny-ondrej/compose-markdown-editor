package me.okonecny.markdowneditor.inline

import me.okonecny.markdowneditor.LinkHandler
import me.okonecny.markdowneditor.Navigation

/**
 * Link that references an anchor somewhere in the document.
 */
internal class InternalAnchorLink(
    private val navigation: Navigation
) : LinkHandler {
    override fun linkActivated(annotationValue: String) {
        navigation.requestScrollToAnchor(annotationValue)
    }

    override fun parseLinkAnnotation(url: String): String? {
        if (!url.startsWith("#")) return null
        return url.substring(1)
    }
}