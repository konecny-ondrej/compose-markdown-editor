package me.okonecny.interactivetext

import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

val LocalNavigation = compositionLocalOf<Navigation> { NopNavigation }

interface Navigation {
    val scrollRequest: ScrollRequest?

    fun registerAnchorTarget(anchor: String, scrollId: Int)
    fun requestScroll(scrollRequest: ScrollRequest)
    suspend fun scrollIfRequested(lazyListState: LazyListState)
}

sealed interface ScrollRequest
data object ScrollPageUp : ScrollRequest
data object ScrollPageDown : ScrollRequest
data class ScrollToComponent(val interactiveComponent: InteractiveComponent) : ScrollRequest
data class ScrollToAnchor(val anchor: String) : ScrollRequest
data class ScrollToIndex(val scrollIndex: Int) : ScrollRequest

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
            is ScrollPageUp -> lazyListState.animateScrollBy(-(lazyListState.layoutInfo.viewportSize.height).toFloat())
            is ScrollPageDown -> lazyListState.animateScrollBy(lazyListState.layoutInfo.viewportSize.height.toFloat())
        }
        scrollRequest = null
    }
}

