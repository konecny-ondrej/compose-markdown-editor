package me.okonecny.markdowneditor

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString

fun <T : Any> Iterable<T>.joinToAnnotatedString(
    separator: AnnotatedString = AnnotatedString(", "),
    prefix: AnnotatedString = AnnotatedString(""),
    postfix: AnnotatedString = AnnotatedString(""),
    limit: Int = -1,
    truncated: AnnotatedString = AnnotatedString("..."),
    filter: (AnnotatedString) -> Boolean = { true },
    transform: ((T) -> AnnotatedString)? = null
): AnnotatedString = buildAnnotatedString {
    var count = 0
    append(prefix)
    for (element in this@joinToAnnotatedString) {
        if (limit < 0 || count <= limit) {
            val transformed = when {
                transform != null -> transform(element)
                element is AnnotatedString -> element
                element is CharSequence -> buildAnnotatedString { append(element) }
                element is Char -> buildAnnotatedString { append(element) }
                else -> AnnotatedString(element.toString())
            }
            if (filter(transformed)) {
                if (count++ > 0) append(separator)
                append(transformed)
            }
        } else break
    }
    if (limit in 0..<count) append(truncated)
    append(postfix)
}

fun <T : Any> Iterable<T>.joinToAnnotatedString(
    separator: String = ", ",
    prefix: String = "",
    postfix: String = "",
    limit: Int = -1,
    truncated: String = "...",
    filter: (AnnotatedString) -> Boolean = { true },
    transform: ((T) -> AnnotatedString)? = null
): AnnotatedString = joinToAnnotatedString(
    AnnotatedString(separator),
    AnnotatedString(prefix),
    AnnotatedString(postfix),
    limit,
    AnnotatedString(truncated),
    filter,
    transform
)

fun AnnotatedString.trim(vararg chars: Char): AnnotatedString {
    val startDiff = length - trimStart(*chars).length
    val endDiff = length - trimEnd(*chars).length
    return subSequence(startDiff, (length - endDiff).coerceAtLeast(startDiff))
}