package com.example.minitangram.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelGeometryTest {
    @Test
    fun builtInLevelGeometryTransformsAcrossCanvasRatios() {
        levels.forEach { level ->
            for (yScale in listOf(.4f, .5f, .58f)) {
                val mapped = level.forCanvas(yScale)
                val targets = centerEditorPoses(mapped.targets, yScale)

                assertEquals(PieceKind.entries.toSet(), targets.keys)
                targets.forEach { (kind, pose) ->
                    val spec = pieceSpecs.first { it.kind == kind }
                    val vertices = transformedVertices(PlayingPiece(spec, pose), yScale)
                    assertEquals(spec.vertices.size, vertices.size)
                    assertTrue("${level.name}@$yScale has invalid vertices", vertices.all {
                        it.x.isFinite() && it.y.isFinite()
                    })
                }
            }
        }
    }
}
