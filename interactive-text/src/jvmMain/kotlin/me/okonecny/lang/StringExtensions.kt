package me.okonecny.lang

import androidx.compose.ui.text.TextRange

fun String.removeRange(range: TextRange) = removeRange(range.start, range.end)

fun String.wordRangeBefore(offset: Int): TextRange {
    require(offset in 0..length) { "offset must be between 0 and $length" }
    var start = (offset - 1).coerceIn(0, lastIndex)
    var consumeWhitespace = get(start).isWhitespace()
    while (start > 0) {
        if (get(start).isWhitespace()) {
            if (!consumeWhitespace) break
        } else {
            consumeWhitespace = false
        }
        start --
    }
    return TextRange(start, offset)
}

fun String.wordRangeAfter(offset: Int): TextRange {
    require(offset in 0..length) { "offset must be between 0 and $length" }
    var end = offset
    var consumeWhitespace = offset < lastIndex && get(offset).isWhitespace()
    while (end < length) {
        if (get(end).isWhitespace()) {
            if (!consumeWhitespace) break
        } else {
            consumeWhitespace = false
        }
        end++
    }
    return TextRange(offset, (end + 1).coerceAtMost(length))
}