package me.okonecny.markdowneditor.inline

import androidx.compose.ui.platform.UriHandler
import me.okonecny.markdowneditor.LinkHandler
import java.net.URI
import java.net.URISyntaxException

class WebLink(private val uriHandler: UriHandler) : LinkHandler {
    private fun canBrowse(url: String): Boolean {
        if (!url.startsWith("http://") && !url.startsWith("https://")) return false
        try {
            URI(url)
        } catch (e: URISyntaxException) {
            return false
        }
        return true
    }

    override fun linkActivated(annotationValue: String) {
        if (!canBrowse(annotationValue)) return
        uriHandler.openUri(annotationValue)
    }

    override fun parseLinkAnnotation(url: String): String? {
        return if (canBrowse(url)) url else null
    }
}