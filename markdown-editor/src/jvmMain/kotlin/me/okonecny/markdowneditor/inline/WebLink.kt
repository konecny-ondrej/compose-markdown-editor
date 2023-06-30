package me.okonecny.markdowneditor.inline

import me.okonecny.markdowneditor.LinkHandler
import java.awt.Desktop
import java.net.URI
import java.net.URISyntaxException

class WebLink : LinkHandler {
    private fun canBrowse(url: String): Boolean {
        if (!url.startsWith("http://") && !url.startsWith("https://")) return false
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) return false
        try {
            URI(url)
        } catch (e: URISyntaxException) {
            return false
        }
        return true
    }

    override fun linkActivated(annotationValue: String) {
        if (!canBrowse(annotationValue)) return
        Desktop.getDesktop().browse(URI(annotationValue))
    }

    override fun parseLinkAnnotation(url: String): String? {
        return if (canBrowse(url)) url else null
    }
}