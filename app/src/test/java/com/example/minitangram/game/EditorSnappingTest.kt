package com.example.minitangram.game

import org.junit.Assert.assertEquals
import org.junit.Test

class EditorSnappingTest {
    @Test
    fun savedContactSurvivesDifferentGameCanvasRatios() {
        val source = Level(-1, "接邊", mapOf(
            PieceKind.LARGE_ONE to Pose(Vec2(.5f, .35f)),
            PieceKind.SQUARE to Pose(Vec2(.54f, .2625f))
        ), .5f)
        for (ratio in listOf(.4f, .7f, 1f)) {
            val mapped = source.forCanvas(ratio)
            fun vertices(kind: PieceKind) = transformedVertices(PlayingPiece(
                pieceSpecs.first { it.kind == kind }, mapped.targets.getValue(kind)), ratio)
            val triangleTop = vertices(PieceKind.LARGE_ONE).minOf { it.y }
            val squareBottom = vertices(PieceKind.SQUARE).maxOf { it.y }
            assertEquals(triangleTop, squareBottom, .00001f)
            val restored = mapped.forCanvas(.5f)
            source.targets.forEach { (kind, pose) ->
                assertEquals(pose.center.y, restored.targets.getValue(kind).center.y, .00001f)
            }
        }
    }

    private val triangle = Pose(Vec2(.5f, .7f))

    @Test
    fun shortSquareEdgeSnapsToMiddleOfLongTriangleEdge() {
        val result = snapEditorPose(PieceKind.SQUARE, Pose(Vec2(.54f, .515f)),
            mapOf(PieceKind.LARGE_ONE to triangle), 1f, .03f)
        assertEquals(.54f, result.center.x, .00001f)
        assertEquals(.525f, result.center.y, .00001f)
    }

    @Test
    fun nearbyEndpointAlignsOnlyAfterEdgeContact() {
        val result = snapEditorPose(PieceKind.SQUARE, Pose(Vec2(.48f, .515f)),
            mapOf(PieceKind.LARGE_ONE to triangle), 1f, .03f)
        assertEquals(.475f, result.center.x, .00001f)
        assertEquals(.525f, result.center.y, .00001f)
    }

    @Test
    fun nearbyVerticesSnapWithoutRoundingToGrid() {
        val pose = Pose(Vec2(.778f, .515f))
        val result = snapEditorPose(PieceKind.SQUARE, pose,
            mapOf(PieceKind.LARGE_ONE to triangle), 1f, .03f)
        assertEquals(.775f, result.center.x, .00001f)
        assertEquals(.525f, result.center.y, .00001f)
        assertEquals(pose, snapEditorPose(PieceKind.SQUARE, pose, emptyMap(), 1f, .03f))
    }

    @Test
    fun twoIndependentEdgesAreSnappedTogether() {
        val horizontal = Pose(Vec2(.525f, .675f))
        val vertical = Pose(Vec2(.475f, .475f), rotation = 90)
        val result = snapEditorPose(
            PieceKind.SQUARE,
            Pose(Vec2(.63f, .49f)),
            mapOf(PieceKind.LARGE_ONE to horizontal, PieceKind.LARGE_TWO to vertical),
            1f,
            .03f
        )
        assertEquals(.65f, result.center.x, .00001f)
        assertEquals(.5f, result.center.y, .00001f)
    }

    @Test
    fun tallCanvasUsesSamePhysicalEdgeGeometry() {
        val result = snapEditorPose(PieceKind.SQUARE, Pose(Vec2(.54f, .2575f)),
            mapOf(PieceKind.LARGE_ONE to triangle.copy(center = Vec2(.5f, .35f))), .5f, .03f)
        assertEquals(.54f, result.center.x, .00001f)
        assertEquals(.2625f, result.center.y, .00001f)
    }

    @Test
    fun diagonalEdgesAlignWithoutRotating() {
        val s = kotlin.math.sqrt(.5f)
        fun rotate(p: Vec2) = Vec2(.5f + (p.x - .5f - (p.y - .5f)) * s,
            .5f + (p.x - .5f + p.y - .5f) * s)
        val pose = Pose(rotate(Vec2(.54f, .515f)), 45)
        val result = snapEditorPose(PieceKind.SQUARE, pose,
            mapOf(PieceKind.LARGE_ONE to Pose(rotate(triangle.center), 45)), 1f, .03f)
        val expected = rotate(Vec2(.54f, .525f))
        assertEquals(expected.x, result.center.x, .00001f)
        assertEquals(expected.y, result.center.y, .00001f)
        assertEquals(45, result.rotation)
    }

    @Test
    fun alignedEdgesAndTidyAreStable() {
        val poses = linkedMapOf(PieceKind.LARGE_ONE to triangle,
            PieceKind.SQUARE to Pose(Vec2(.54f, .525f)))
        val result = tidyEditorPoses(poses, 1f, .03f)
        poses.forEach { (kind, pose) ->
            assertEquals(pose.center.x, result.getValue(kind).center.x, .00001f)
            assertEquals(pose.center.y, result.getValue(kind).center.y, .00001f)
        }
    }

    @Test
    fun centerEditorPosesAlignsWholeBoundsOnBothAxes() {
        val poses = mapOf(
            PieceKind.LARGE_ONE to Pose(Vec2(.2f, .25f)),
            PieceKind.SQUARE to Pose(Vec2(.35f, .3f))
        )
        val centered = centerEditorPoses(poses, 1f)
        val vertices = centered.map { (kind, pose) ->
            transformedVertices(PlayingPiece(pieceSpecs.first { it.kind == kind }, pose))
        }.flatten()
        assertEquals(.5f, (vertices.minOf { it.x } + vertices.maxOf { it.x }) / 2f, .00001f)
        assertEquals(TARGET_AREA_BOTTOM / 2f, (vertices.minOf { it.y } + vertices.maxOf { it.y }) / 2f, .00001f)
    }
}
