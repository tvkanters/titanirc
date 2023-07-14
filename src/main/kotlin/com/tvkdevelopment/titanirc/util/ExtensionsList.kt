package com.tvkdevelopment.titanirc.util

fun <E> MutableList<E>.addWithLimit(element: E, limit: Int) {
    if (limit == 0) {
        return
    }
    if (contains(element)) {
        this -= element
    } else if (size == limit) {
        removeFirst()
    }
    this += element
}

fun <K, V> LinkedHashMap<K, V>.addWithLimit(key: K, value: V, limit: Int) {
    if (limit == 0) {
        return
    }
    if (size == limit) {
        remove(keys.first())
    }
    put(key, value)
}