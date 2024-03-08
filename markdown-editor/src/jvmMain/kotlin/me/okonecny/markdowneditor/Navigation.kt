package me.okonecny.markdowneditor

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

interface Navigation {
    val scrollRequest: Int?

    fun registerAnchorTarget(anchor: String, scrollId: Int)
    fun requestScrollToAnchor(anchor: String)
}

internal object NopNavigation : Navigation {
    override val scrollRequest: Int? = null

    override fun registerAnchorTarget(anchor: String, scrollId: Int) {
        // NOP
    }

    override fun requestScrollToAnchor(anchor: String) {
        // NOP
    }
}

internal class ScrollableNavigation : Navigation {
    private val anchorIndex = mutableMapOf<String, Int>()
    override var scrollRequest: Int? by mutableStateOf(null)
        private set
    var currentScrollId: Int? = null

    override fun registerAnchorTarget(anchor: String, scrollId: Int) {
        anchorIndex.putIfAbsent(anchor, scrollId)
    }

    override fun requestScrollToAnchor(anchor: String) {
        val scrollId = anchorIndex[anchor] ?: return
        scrollRequest = scrollId
    }

    suspend fun scrollIfRequested(lazyListState: LazyListState) {
        val requestedId = scrollRequest ?: return
        lazyListState.animateScrollToItem(requestedId)
        scrollRequest = null
    }
}

