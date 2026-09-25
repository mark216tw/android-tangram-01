package com.example.minitangram.game

import com.example.minitangram.data.UserProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.hypot
import kotlin.math.sqrt

class TangramModelsTest {
    @Test
    fun soundIsEnabledByDefault() {
        assertTrue(UserProgress().soundEnabled)
    }

    @Test
    fun builtInLevelsAreVisibleByDefault() {
        assertFalse(UserProgress().hideBuiltInLevels)
    }

    @Test
    fun beginnerDifficultyIsEnabledByDefault() {
        assertEquals(Difficulty.BEGINNER, UserProgress().difficulty)
    }

    @Test
    fun angleDistance_wrapsAcrossZero() {
        assertEquals(0, angleDistance(0, 360))
        assertEquals(45, angleDistance(0, 315))
        assertEquals(90, angleDistance(315, 45))
    }

    @Test
    fun containsPoint_detectsInsideAndOutsidePolygon() {
        val square = listOf(Vec2(0f, 0f), Vec2(1f, 0f), Vec2(1f, 1f), Vec2(0f, 1f))

        assertTrue(containsPoint(square, Vec2(.5f, .5f)))
        assertFalse(containsPoint(square, Vec2(1.5f, .5f)))
    }

    @Test
    fun everyLevelDefinesEachPieceExactlyOnce() {
        assertEquals(20, levels.size)
        assertEquals(
            listOf("蛇", "老鷹", "蝴蝶", "恐龍", "狐狸", "蝙蝠", "天鵝", "蝦子", "北極熊", "趴著的貓", "馬", "狐狸", "鯊魚", "鴨子", "狗", "鯨魚", "螃蟹", "烏龜", "站立的貓", "牛"),
            levels.map { it.name }
        )
        assertEquals((1..levels.size).toList(), levels.map { it.id })
        levels.forEach { level ->
            assertEquals(PieceKind.entries.toSet(), level.targets.keys)
            assertEquals(.5913853f, level.canvasYScale)
            assertTrue(level.targets.values.all { it.rotation % 45 == 0 })
            assertTrue(level.targets.values.all { it.center.x in 0f..1f && it.center.y in 0f..1f })
        }
    }

    @Test
    fun builtInLevelProgressionFollowsLevelOrder() {
        assertTrue(isBuiltInLevelUnlocked(levels.first().id, levels.first().id))
        assertFalse(isBuiltInLevelUnlocked(levels[1].id, levels.first().id))
        assertEquals(levels[1], nextBuiltInLevel(levels.first().id))
        assertEquals(null, nextBuiltInLevel(levels.last().id))
    }

    @Test
    fun transformedVertices_respectsFlipAndRotation() {
        val spec = pieceSpecs.first { it.kind == PieceKind.PARALLELOGRAM }
        val normal = transformedVertices(PlayingPiece(spec, Pose(Vec2(.5f, .5f))))
        val flipped = transformedVertices(PlayingPiece(spec, Pose(Vec2(.5f, .5f), flipped = true)))

        assertEquals(normal.first().x, 1f - flipped.first().x, .0001f)
        assertEquals(normal.first().y, flipped.first().y, .0001f)
    }

    @Test
    fun pieceAreas_followStandardTangramRatios() {
        val areas = pieceSpecs.associate { it.kind to polygonArea(it.vertices) }
        val smallArea = areas.getValue(PieceKind.SMALL_ONE)

        assertEquals(4f, areas.getValue(PieceKind.LARGE_ONE) / smallArea, .0001f)
        assertEquals(4f, areas.getValue(PieceKind.LARGE_TWO) / smallArea, .0001f)
        assertEquals(2f, areas.getValue(PieceKind.MEDIUM) / smallArea, .0001f)
        assertEquals(2f, areas.getValue(PieceKind.SQUARE) / smallArea, .0001f)
        assertEquals(2f, areas.getValue(PieceKind.PARALLELOGRAM) / smallArea, .0001f)
        assertEquals(1f, areas.getValue(PieceKind.SMALL_TWO) / smallArea, .0001f)
    }

    @Test
    fun triangles_areRightIsoscelesAndCentered() {
        pieceSpecs.filter { it.vertices.size == 3 }.forEach { piece ->
            val sides = piece.vertices.indices.map { index ->
                val a = piece.vertices[index]
                val b = piece.vertices[(index + 1) % piece.vertices.size]
                hypot((a.x - b.x).toDouble(), (a.y - b.y).toDouble()).toFloat()
            }.sorted()

            assertEquals(sides[0], sides[1], .0001f)
            assertEquals(sides[2], sides[0] * sqrt(2f), .0001f)
            assertEquals(0f, piece.vertices.sumOf { it.x.toDouble() }.toFloat(), .0001f)
            assertEquals(0f, piece.vertices.sumOf { it.y.toDouble() }.toFloat(), .0001f)
        }
    }

    @Test
    fun normalizedYScale_preservesShapeOnTallCanvas() {
        val piece = PlayingPiece(pieceSpecs.first(), Pose(Vec2(.5f, .5f), rotation = 45))
        val vertices = transformedVertices(piece, normalizedYScale = .5f)
        val pixelSides = vertices.indices.map { index ->
            val a = vertices[index]
            val b = vertices[(index + 1) % vertices.size]
            hypot(((a.x - b.x) * 400).toDouble(), ((a.y - b.y) * 800).toDouble()).toFloat()
        }.sorted()

        assertEquals(pixelSides[0], pixelSides[1], .001f)
        assertEquals(pixelSides[2], pixelSides[0] * sqrt(2f), .001f)
    }

    @Test
    fun isotropicVertices_areIndependentOfCanvasRatio() {
        val sourceScale = .5f
        val sourcePiece = PlayingPiece(
            pieceSpecs.first { it.kind == PieceKind.PARALLELOGRAM },
            Pose(Vec2(.42f, .31f), rotation = 45, flipped = true)
        )
        val targetScale = .8f
        val targetPose = Level(
            id = 0,
            name = "test",
            targets = mapOf(sourcePiece.spec.kind to sourcePiece.pose),
            canvasYScale = sourceScale
        ).forCanvas(targetScale).targets.getValue(sourcePiece.spec.kind)
        val source = isotropicVertices(sourcePiece, sourceScale)
        val target = isotropicVertices(PlayingPiece(sourcePiece.spec, targetPose), targetScale)
        val translationY = target.first().y - source.first().y

        source.zip(target).forEach { (sourceVertex, targetVertex) ->
            assertEquals(sourceVertex.x, targetVertex.x, .0001f)
            assertEquals(sourceVertex.y + translationY, targetVertex.y, .0001f)
        }
    }

    @Test
    fun equalSizedTriangles_canUseEachOthersTargets() {
        assertTrue(PieceKind.LARGE_ONE.canUseTarget(PieceKind.LARGE_TWO))
        assertTrue(PieceKind.LARGE_TWO.canUseTarget(PieceKind.LARGE_ONE))
        assertTrue(PieceKind.SMALL_ONE.canUseTarget(PieceKind.SMALL_TWO))
        assertTrue(PieceKind.SMALL_TWO.canUseTarget(PieceKind.SMALL_ONE))
        assertFalse(PieceKind.LARGE_ONE.canUseTarget(PieceKind.SMALL_ONE))
        assertFalse(PieceKind.MEDIUM.canUseTarget(PieceKind.LARGE_ONE))
    }

    @Test
    fun rotationMatching_respectsShapeSymmetry() {
        assertTrue(rotationMatches(PieceKind.SQUARE, 90, 0))
        assertTrue(rotationMatches(PieceKind.SQUARE, 315, 45))
        assertTrue(rotationMatches(PieceKind.PARALLELOGRAM, 180, 0))
        assertFalse(rotationMatches(PieceKind.PARALLELOGRAM, 90, 0))
        assertFalse(rotationMatches(PieceKind.MEDIUM, 180, 0))
    }

    @Test
    fun parallelogramStillRequiresCorrectFlip() {
        val target = Pose(Vec2(.5f, .5f), rotation = 0, flipped = true)

        assertTrue(orientationMatches(PieceKind.PARALLELOGRAM, target.copy(rotation = 180), target))
        assertFalse(orientationMatches(PieceKind.PARALLELOGRAM, target.copy(flipped = false), target))
    }

    private fun polygonArea(vertices: List<Vec2>): Float {
        val twiceArea = vertices.indices.sumOf { index ->
            val a = vertices[index]
            val b = vertices[(index + 1) % vertices.size]
            (a.x * b.y - b.x * a.y).toDouble()
        }
        return kotlin.math.abs(twiceArea.toFloat()) / 2f
    }
}
