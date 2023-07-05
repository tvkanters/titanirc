package com.tvkdevelopment.titanirc.discord

class MutableIndexedRegistry<K, V, N>(private val normalize: (V) -> N?) {
    private val mutableItemsByKey = mutableMapOf<K, V>()
    val itemsByKey = mutableItemsByKey

    val itemsByNormalizedValue = mutableMapOf<N, K>()
        get() {
            if (itemsByNormalizedValueInvalidated) {
                mutableItemsByKey.forEach { (key, value) ->
                    normalize(value)?.let { field[it] = key }
                }
                itemsByNormalizedValueInvalidated = false
            }
            return field
        }

    private var itemsByNormalizedValueInvalidated = false

    operator fun set(key: K, value: V) {
        mutableItemsByKey[key] = value
        itemsByNormalizedValueInvalidated = true
    }
}