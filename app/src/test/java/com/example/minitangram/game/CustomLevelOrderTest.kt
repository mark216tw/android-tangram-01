package com.example.minitangram.game

import org.junit.Assert.assertEquals
import org.junit.Test

class CustomLevelOrderTest {
    private val original = listOf(-1, -2, -3).map { Level(it, "關卡 $it", emptyMap()) }

    @Test
    fun editingMiddleLevelPreservesPositionAndReplacesData() {
        val edited = original[1].copy(name = "新名稱", targets = mapOf(PieceKind.SQUARE to Pose(Vec2(.5f, .5f))))
        val result = original.saveInPlace(edited)
        assertEquals(listOf(-1, -2, -3), result.map { it.id })
        assertEquals(edited, result[1])
        assertEquals(result, result.saveInPlace(edited))
    }

    @Test
    fun newLevelIsAppendedWithoutChangingExistingOrder() {
        val added = Level(-4, "新增", emptyMap())
        assertEquals(original + added, original.saveInPlace(added))
    }
}
