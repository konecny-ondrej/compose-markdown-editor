package me.okonecny.lang

fun <T> Iterable<T>.only(errorMsg: String = "There must be at exactly one element."): T {
    val iterator = iterator()
    if (!iterator.hasNext())throw IllegalStateException(errorMsg)
    val elem = iterator.next()
    if (iterator.hasNext()) throw IllegalStateException(errorMsg)
    return elem
}

fun <T> Iterable<T>.onlyOrNull(errorMsg: String = "There must be at most one element."): T? {
    val iterator = iterator()
    if (!iterator.hasNext()) return null
    val elem = iterator.next()
    if (iterator.hasNext()) throw IllegalStateException(errorMsg)
    return elem
}