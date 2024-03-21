package me.okonecny.interactivetext

import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.abs

val LocalNavigation = compositionLocalOf<Navigation> { NopNavigation }

interface Navigation {
    val scrollRequest: ScrollRequest?

    fun registerAnchorTarget(anchor: String, scrollId: Int)
    fun requestScroll(scrollRequest: ScrollRequest)
    suspend fun scrollIfRequested(lazyListState: LazyListState)
}

sealed interface ScrollRequest
class ScrollPageUp(val fraction: Float = 1.0f) : ScrollRequest
class ScrollPageDown(val fraction: Float = 1.0f) : ScrollRequest
data class ScrollToComponent(val interactiveComponent: InteractiveComponent) : ScrollRequest
data class ScrollToAnchor(val anchor: String) : ScrollRequest
data class ScrollToIndex(val scrollIndex: Int) : ScrollRequest
data class ScrollToMakeVisible(val range: IntRange) : ScrollRequest

internal object NopNavigation : Navigation {
    override val scrollRequest: ScrollRequest? = null

    override fun registerAnchorTarget(anchor: String, scrollId: Int) {
        // NOP
    }

    override fun requestScroll(scrollRequest: ScrollRequest) {
        // NOP
    }

    override suspend fun scrollIfRequested(lazyListState: LazyListState) {
        // NOP
    }
}

internal class ScrollableNavigation : Navigation {
    private val anchorIndex = mutableMapOf<String, Int>()
    override var scrollRequest: ScrollRequest? by mutableStateOf(null)
        private set

    override fun registerAnchorTarget(anchor: String, scrollId: Int) {
        anchorIndex.putIfAbsent(anchor, scrollId)
    }

    override fun requestScroll(scrollRequest: ScrollRequest) {
        this.scrollRequest = scrollRequest
    }

    override suspend fun scrollIfRequested(lazyListState: LazyListState) {
        val request = scrollRequest ?: return
        when (request) {
            is ScrollToIndex -> lazyListState.animateScrollToItem(request.scrollIndex)
            is ScrollToComponent -> lazyListState.animateScrollToItem(
                request.interactiveComponent.scrollIndex ?: return
            )

            is ScrollToAnchor -> lazyListState.animateScrollToItem(anchorIndex[request.anchor] ?: return)
            is ScrollPageUp -> lazyListState.animateScrollBy(-(lazyListState.layoutInfo.viewportSize.height * request.fraction))
            is ScrollPageDown -> lazyListState.animateScrollBy(lazyListState.layoutInfo.viewportSize.height * request.fraction)
            is ScrollToMakeVisible -> scrollToMakeVisible(request.range, lazyListState)
        }
        scrollRequest = null
    }

    private suspend fun scrollToMakeVisible(requestVisible: IntRange, lazyListState: LazyListState) {
        val layoutInfo = lazyListState.layoutInfo
        val viewport = IntRange(layoutInfo.viewportStartOffset, layoutInfo.viewportEndOffset)
        // If the requested range is larger than the viewport, don't do anything.
        // We won't be able to make it visible anyway.
        if (requestVisible.span > viewport.span) return
        // If the requested range really is visible as a whole, don't do anything.
        if (requestVisible.first >= viewport.first && requestVisible.last <= viewport.last) return

        if (requestVisible.first < viewport.first) {
            lazyListState.animateScrollBy((requestVisible.first - viewport.first).toFloat())
        } else if (requestVisible.last > viewport.last) {
            lazyListState.animateScrollBy((requestVisible.last - viewport.last).toFloat())
        }
    }

    private val IntRange.span get() = abs(first - last)
}

