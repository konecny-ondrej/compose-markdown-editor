package me.okonecny.interactivetext

import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NavigableLazyColumn(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical =
        if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    navigation: Navigation = LocalNavigation.current,
    content: NavigableLazyListScope.() -> Unit
) {
    LazyColumn(
        modifier, state, contentPadding, reverseLayout, verticalArrangement, horizontalAlignment, flingBehavior,
        userScrollEnabled
    ) {
        NavigableLazyListScope(this, navigation).content()
    }
    LaunchedEffect(navigation.scrollRequest) {
        navigation.scrollIfRequested(state)
    }
}

val LocalScrollIndex = compositionLocalOf<Int?> { null }

data class NavigableLazyListScope(
    private val lazyListScope: LazyListScope,
    val navigation: Navigation
) {
    private var itemCount = 0

    private fun makeContent(
        scrollId: Int,
        content: @Composable LazyItemScope.() -> Unit
    ): @Composable LazyItemScope.() -> Unit = {
        CompositionLocalProvider(LocalScrollIndex provides scrollId) {
            content()
        }
    }

    fun item(
        key: Any? = null,
        contentType: Any? = null,
        content: @Composable LazyItemScope.() -> Unit
    ) {
        lazyListScope.item(key, contentType, makeContent(itemCount++, content))
    }
}