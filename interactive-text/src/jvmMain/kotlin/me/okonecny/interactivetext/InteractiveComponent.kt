package me.okonecny.interactivetext

import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import kotlin.reflect.KClass

/**
 * Class representing an interactive GUI element.
 * Best created in onGloballyPositioned modifier.
 */
data class InteractiveComponent(
    /**
     * ID of the interactive component.
     * @see InteractiveScope.rememberInteractiveId
     */
    val id: InteractiveId,
    /**
     * Global layout coordinates used to sort the interactive components for the purpose of prev/next navigation.
     * @see androidx.compose.ui.layout.onGloballyPositioned
     */
    val layoutCoordinates: LayoutCoordinates,
    /**
     * Text range of the displayed text if the represented component is a component displaying text.
     * @see androidx.compose.ui.text.TextRange
     */
    val visualTextRange: TextRange,
    /**
     * Maps ranges of displayed text to ranges of source text and vice-versa. Useful for writing editors.
     */
    val textMapping: TextMapping,
    /**
     * Result of laying out the text if this is a component displaying text.
     * Used for cursor movement.
     */
    val textLayoutResult: TextLayoutResult?,
    /**
     * Any kind of data one might want to attach to this component.
     */
    val userData: UserData = UserData.empty
) {
    /**
     * True if the component contains any text. False otherwise.
     */
    val hasText: Boolean get() = textLayoutResult != null && !visualTextRange.collapsed

    /**
     * True if the text contained in this component has more than one line.
     * False if the component has no text or the contained text is just one line.
     */
    val isMultiline: Boolean get() = hasText && textLayoutResult != null && textLayoutResult.lineCount > 1

    /**
     * Reads user data.
     */
    inline operator fun <reified T : Any> get(type: KClass<T>): T = userData[type]

    /**
     * Check if user data of the specified type are attached.
     */
    inline fun <reified T : Any> hasData(): Boolean = userData.hasData<T>()

    /**
     * Creates a new instance of this component with the user data attached.
     */
    fun <T : Any> withData(type: KClass<T>, value: T): InteractiveComponent =
        copy(userData = userData.withData(type, value))
}

data class UserData constructor(
    val dataByType: Map<KClass<*>, Any> = emptyMap()
) {
    companion object {
        val empty = UserData()
    }

    inline operator fun <reified T : Any> get(type: KClass<T>): T {
        val data = dataByType[type]
        if (data is T) return data
        throw NoSuchElementException("No data of type %s are present.".format(type))
    }

    inline fun <reified T : Any> hasData(): Boolean {
        return dataByType[T::class] is T
    }

    fun <T : Any> withData(type: KClass<T>, value: T): UserData {
        return UserData(dataByType + mapOf(type to value))
    }
}