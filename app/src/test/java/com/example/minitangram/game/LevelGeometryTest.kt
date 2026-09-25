package com.example.minitangram.game

import org.junit.Assert.fail
import org.junit.Test
import kotlin.math.hypot

class LevelGeometryTest {
    private val epsilon = .0005

    @Test
    fun builtInLevelsDoNotOverlapAndAreConnected() {
        val failures = mutableListOf<String>()
        levels.forEach { level ->
            for (yScale in listOf(.4f, .5f, .58f)) {
                val mapped = level.forCanvas(yScale)
                val targets = centerEditorPoses(mapped.targets, yScale)
                val canvasPolygons = PieceKind.entries.map { kind ->
                    kind to transformedVertices(PlayingPiece(pieceSpecs.first { it.kind == kind }, targets.getValue(kind)), yScale)
                }
                val polygons = canvasPolygons.map { (kind, vertices) ->
                    kind to vertices.map { Vec2(it.x, it.y / yScale) }
                }
                polygons.indices.forEach { first ->
                    for (second in first + 1 until polygons.size) {
                        val depth = overlapDepth(polygons[first].second, polygons[second].second)
                        if (depth > epsilon) {
                            failures += "${level.name}@$yScale: ${polygons[first].first} overlaps ${polygons[second].first} (${"%.4f".format(depth)})"
                        }
                    }
                }
                val visited = mutableSetOf(0)
                val pending = ArrayDeque<Int>().apply { add(0) }
                while (pending.isNotEmpty()) {
                    val current = pending.removeFirst()
                    polygons.indices.filter { it !in visited && boundariesTouch(polygons[current].second, polygons[it].second) }
                        .forEach { visited += it; pending += it }
                }
                if (visited.size != polygons.size) failures += "${level.name}@$yScale: disconnected pieces (${visited.size}/7 connected)"
                val canvasVertices = canvasPolygons.flatMap { it.second }
                if (canvasVertices.any { it.x < -epsilon || it.x > 1.0 + epsilon || it.y < -epsilon || it.y > TARGET_AREA_BOTTOM + epsilon }) {
                    failures += "${level.name}@$yScale: exceeds target area x=${"%.3f".format(canvasVertices.minOf { it.x })}..${"%.3f".format(canvasVertices.maxOf { it.x })}, y=${"%.3f".format(canvasVertices.minOf { it.y })}..${"%.3f".format(canvasVertices.maxOf { it.y })}"
                }
            }
        }
        if (failures.isNotEmpty()) fail(failures.joinToString("\n"))
    }

    private fun overlapDepth(first: List<Vec2>, second: List<Vec2>): Double {
        val axes = edges(first).map { normal(it) } + edges(second).map { normal(it) }
        return axes.minOf { axis ->
            val firstProjection = first.map { dot(it, axis) }
            val secondProjection = second.map { dot(it, axis) }
            minOf(firstProjection.max(), secondProjection.max()) - maxOf(firstProjection.min(), secondProjection.min())
        }
    }

    private fun boundariesTouch(first: List<Vec2>, second: List<Vec2>): Boolean =
        edges(first).any { a -> edges(second).any { b -> segmentDistance(a, b) <= epsilon } }

    private fun edges(vertices: List<Vec2>): List<Pair<Vec2, Vec2>> = vertices.indices.map { index ->
        vertices[index] to vertices[(index + 1) % vertices.size]
    }

    private fun normal(edge: Pair<Vec2, Vec2>): Vec2 {
        val dx = edge.second.x - edge.first.x
        val dy = edge.second.y - edge.first.y
        val length = hypot(dx.toDouble(), dy.toDouble()).coerceAtLeast(1e-12)
        return Vec2((-dy / length).toFloat(), (dx / length).toFloat())
    }

    private fun dot(point: Vec2, axis: Vec2): Double = point.x * axis.x.toDouble() + point.y * axis.y.toDouble()

    private fun segmentDistance(first: Pair<Vec2, Vec2>, second: Pair<Vec2, Vec2>): Double {
        if (segmentsIntersect(first, second)) return 0.0
        return minOf(
            pointSegmentDistance(first.first, second),
            pointSegmentDistance(first.second, second),
            pointSegmentDistance(second.first, first),
            pointSegmentDistance(second.second, first)
        )
    }

    private fun segmentsIntersect(first: Pair<Vec2, Vec2>, second: Pair<Vec2, Vec2>): Boolean {
        fun cross(a: Vec2, b: Vec2, c: Vec2): Double =
            (b.x - a.x) * (c.y - a.y).toDouble() - (b.y - a.y) * (c.x - a.x).toDouble()
        val a = cross(first.first, first.second, second.first)
        val b = cross(first.first, first.second, second.second)
        val c = cross(second.first, second.second, first.first)
        val d = cross(second.first, second.second, first.second)
        if (a * b < -epsilon && c * d < -epsilon) return true
        fun onSegment(point: Vec2, segment: Pair<Vec2, Vec2>): Boolean =
            point.x >= minOf(segment.first.x, segment.second.x) - epsilon &&
                point.x <= maxOf(segment.first.x, segment.second.x) + epsilon &&
                point.y >= minOf(segment.first.y, segment.second.y) - epsilon &&
                point.y <= maxOf(segment.first.y, segment.second.y) + epsilon
        return (kotlin.math.abs(a) <= epsilon && onSegment(second.first, first)) ||
            (kotlin.math.abs(b) <= epsilon && onSegment(second.second, first)) ||
            (kotlin.math.abs(c) <= epsilon && onSegment(first.first, second)) ||
            (kotlin.math.abs(d) <= epsilon && onSegment(first.second, second))
    }

    private fun pointSegmentDistance(point: Vec2, segment: Pair<Vec2, Vec2>): Double {
        val dx = segment.second.x - segment.first.x
        val dy = segment.second.y - segment.first.y
        val lengthSquared = dx * dx + dy * dy
        if (lengthSquared <= 1e-12) return hypot((point.x - segment.first.x).toDouble(), (point.y - segment.first.y).toDouble())
        val t = (((point.x - segment.first.x) * dx + (point.y - segment.first.y) * dy) / lengthSquared).coerceIn(0f, 1f)
        return hypot((point.x - segment.first.x - dx * t).toDouble(), (point.y - segment.first.y - dy * t).toDouble())
    }
}
