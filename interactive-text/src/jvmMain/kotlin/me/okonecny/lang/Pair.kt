package me.okonecny.lang

fun <A, B> Pair<A, A>.map(f: (A) -> B): Pair<B, B> = Pair(f(first), f(second))