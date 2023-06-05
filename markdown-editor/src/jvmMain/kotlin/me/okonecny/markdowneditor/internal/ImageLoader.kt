package me.okonecny.markdowneditor.internal

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.unit.Density
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject
import org.xml.sax.InputSource
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.file.Path

/**
 * Loads images from the Internet or from local files.
 */
@Inject
@MarkdownEditorScope
internal class ImageLoader(
    lazyHttpClient: Lazy<HttpClient>
) {
    private val httpClient by lazyHttpClient
    private val imageCache = mutableMapOf<String, Painter>()

    private val String.isHttp get() = startsWith("http://") || startsWith("https://")
    private fun String.fullPath(basePath: Path): String = if (isHttp) this else basePath.resolve(this).toString()

    fun unloadedImage(url: String, basePath: Path): Painter? {
        val fullPath = url.fullPath(basePath)
        return synchronized(imageCache) {
            imageCache[fullPath]
        }
    }

    suspend fun load(url: String, basePath: Path): Painter {
        val fullPath = url.fullPath(basePath)
        synchronized(imageCache) {
            val cachedImage = imageCache[fullPath]
            if (cachedImage != null) return@load cachedImage
        }
        val loadedImage = BitmapPainter(if (url.isHttp) withContext(Dispatchers.IO) {
            loadImageBitmap(httpClient, fullPath)
        } else {
            loadImageBitmap(File(fullPath))
        })
        synchronized(imageCache) {
            imageCache.putIfAbsent(fullPath, loadedImage)
        }
        return loadedImage
    }
}

private fun loadImageBitmap(file: File): ImageBitmap =
    file.inputStream().buffered().use(::loadImageBitmap)

private fun loadSvgPainter(file: File, density: Density): Painter =
    file.inputStream().buffered().use { androidx.compose.ui.res.loadSvgPainter(it, density) }

private fun loadXmlImageVector(file: File, density: Density): ImageVector =
    file.inputStream().buffered().use { androidx.compose.ui.res.loadXmlImageVector(InputSource(it), density) }

/* Loading from network with Ktor client API (https://ktor.io/docs/client.html). */

private suspend fun loadImageBitmap(httpClient: HttpClient, url: String): ImageBitmap =
    urlStream(httpClient, url).use(::loadImageBitmap)

private suspend fun loadSvgPainter(httpClient: HttpClient, url: String, density: Density): Painter =
    urlStream(httpClient, url).use { androidx.compose.ui.res.loadSvgPainter(it, density) }

private suspend fun loadXmlImageVector(httpClient: HttpClient, url: String, density: Density): ImageVector =
    urlStream(httpClient, url).use { androidx.compose.ui.res.loadXmlImageVector(InputSource(it), density) }

private suspend fun urlStream(httpClient: HttpClient, url: String) = ByteArrayInputStream(httpClient.get(url).body())