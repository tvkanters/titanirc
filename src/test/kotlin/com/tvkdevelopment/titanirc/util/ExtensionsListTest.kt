package com.tvkdevelopment.titanirc.util

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ExtensionsListTest {

    @Test
    fun testListAddWithLimit() {
        // GIVEN
        val list = mutableListOf<Int>()

        // WHEN
        list.addWithLimit(1, 2)

        // THEN
        assertEquals(listOf(1), list)

        // WHEN
        list.addWithLimit(2, 2)

        // THEN
        assertEquals(listOf(1, 2), list)

        // WHEN
        list.addWithLimit(3, 2)

        // THEN
        assertEquals(listOf(2, 3), list)
    }

    @Test
    fun testMapAddWithLimit() {
        // GIVEN
        val map = LinkedHashMap<String, Int>()

        // WHEN
        map.addWithLimit("a", 1, 2)

        // THEN
        assertEquals(mapOf("a" to 1), map)

        // WHEN
        map.addWithLimit("b", 2, 2)

        // THEN
        assertEquals(mapOf("a" to 1, "b" to 2), map)

        // WHEN
        map.addWithLimit("c", 3, 2)

        // THEN
        assertEquals(mapOf("b" to 2, "c" to 3), map)
    }
}