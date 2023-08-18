package me.okonecny.markdowneditor.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize

@Composable
internal fun MeasuringLayout(
    modifier: Modifier = Modifier,
    measuredContent: @Composable () -> Unit,
    dependentContent: @Composable (measuredSize: DpSize) -> Unit
) {

    val density = LocalDensity.current
    SubcomposeLayout(
        modifier = modifier
    ) { constraints: Constraints ->
        // Subcompose(compose only a section) main content and get Placeable
        val measuredPlaceable: Placeable = subcompose(LayoutSlot.Measured, measuredContent)
            .only()
            .measure(constraints.copy(minWidth = 0, minHeight = 0))

        val measuredSize: DpSize = density.run {
            IntSize(measuredPlaceable.width, measuredPlaceable.height).toSize().toDpSize()
        }
        val dependentPlaceable: Placeable = subcompose(LayoutSlot.Dependent) {
            dependentContent(measuredSize)
        }.only().measure(constraints)

        layout(dependentPlaceable.width, dependentPlaceable.height) {
            dependentPlaceable.placeRelative(0, 0)
        }
    }
}

private enum class LayoutSlot {
    Measured,
    Dependent
}

private fun <T> Collection<T>.only(): T {
    require(size == 1) { "There must be only one element in the list." }
    return first()
}