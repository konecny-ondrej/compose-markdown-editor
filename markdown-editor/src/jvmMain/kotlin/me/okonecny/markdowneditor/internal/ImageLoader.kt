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

/**
 * Loads images from the Internet or from local files.
 */
@Inject
internal class ImageLoader(
    lazyHttpClient: Lazy<HttpClient>
) {
    private val httpClient by lazyHttpClient

    suspend fun load(url: String): Painter {
        return withContext(Dispatchers.IO) {
            // TODO: load local images from disk if the URL does not start with "http://" or "https://".
            BitmapPainter(loadImageBitmap(httpClient, url))
        }
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