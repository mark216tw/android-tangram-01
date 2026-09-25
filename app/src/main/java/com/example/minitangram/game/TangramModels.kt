package com.example.minitangram.game

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Vec2(val x: Float, val y: Float) {
    operator fun plus(other: Vec2) = Vec2(x + other.x, y + other.y)
    operator fun minus(other: Vec2) = Vec2(x - other.x, y - other.y)
}

enum class PieceKind { LARGE_ONE, LARGE_TWO, MEDIUM, SMALL_ONE, SMALL_TWO, SQUARE, PARALLELOGRAM }

data class PieceSpec(val kind: PieceKind, val vertices: List<Vec2>, val color: Long)
data class Pose(val center: Vec2, val rotation: Int = 0, val flipped: Boolean = false)
data class Level(val id: Int, val name: String, val targets: Map<PieceKind, Pose>, val canvasYScale: Float? = null)

const val TARGET_AREA_BOTTOM = .68f

fun Level.forCanvas(yScale: Float): Level {
    val source = canvasYScale ?: return this
    return copy(targets = targets.mapValues { (_, pose) ->
        pose.copy(center = Vec2(pose.center.x, .4f + (pose.center.y - .4f) * yScale / source))
    }, canvasYScale = yScale)
}
enum class Difficulty { BEGINNER, ADVANCED }
enum class PieceColorTheme { CLASSIC, BRIGHT, PASTEL, OCEAN, SUNSET, MONOCHROME }
data class PlayingPiece(
    val spec: PieceSpec,
    val pose: Pose,
    val snapped: Boolean = false,
    val snappedTarget: PieceKind? = null
)

private const val SMALL_LEG = .15f
private val MEDIUM_LEG = SMALL_LEG * sqrt(2f)
private const val LARGE_LEG = SMALL_LEG * 2f

private fun rightTriangle(leg: Float) = listOf(
    Vec2(-leg / 3f, -leg / 3f),
    Vec2(leg * 2f / 3f, -leg / 3f),
    Vec2(-leg / 3f, leg * 2f / 3f)
)

private fun square(side: Float) = listOf(
    Vec2(-side / 2f, -side / 2f),
    Vec2(side / 2f, -side / 2f),
    Vec2(side / 2f, side / 2f),
    Vec2(-side / 2f, side / 2f)
)

private fun parallelogram(shortSide: Float): List<Vec2> {
    val offset = shortSide / sqrt(2f)
    val longSide = shortSide * sqrt(2f)
    return listOf(
        Vec2(-(longSide + offset) / 2f, -offset / 2f),
        Vec2((longSide - offset) / 2f, -offset / 2f),
        Vec2((longSide + offset) / 2f, offset / 2f),
        Vec2(-(longSide - offset) / 2f, offset / 2f)
    )
}

val pieceSpecs = listOf(
    PieceSpec(PieceKind.LARGE_ONE, rightTriangle(LARGE_LEG), 0xFFB83A2E),
    PieceSpec(PieceKind.LARGE_TWO, rightTriangle(LARGE_LEG), 0xFFD37B2C),
    PieceSpec(PieceKind.MEDIUM, rightTriangle(MEDIUM_LEG), 0xFF477665),
    PieceSpec(PieceKind.SMALL_ONE, rightTriangle(SMALL_LEG), 0xFF365C7D),
    PieceSpec(PieceKind.SMALL_TWO, rightTriangle(SMALL_LEG), 0xFF80629A),
    PieceSpec(PieceKind.SQUARE, square(SMALL_LEG), 0xFFC3A33B),
    PieceSpec(PieceKind.PARALLELOGRAM, parallelogram(SMALL_LEG), 0xFF6E4937)
)

fun PieceKind.colorFor(theme: PieceColorTheme): Long = when (theme) {
    PieceColorTheme.CLASSIC -> pieceSpecs.first { it.kind == this }.color
    PieceColorTheme.BRIGHT -> when (this) {
        PieceKind.LARGE_ONE -> 0xFFE53935
        PieceKind.LARGE_TWO -> 0xFFFF8F00
        PieceKind.MEDIUM -> 0xFF00A878
        PieceKind.SMALL_ONE -> 0xFF1976D2
        PieceKind.SMALL_TWO -> 0xFF7E57C2
        PieceKind.SQUARE -> 0xFFFFD600
        PieceKind.PARALLELOGRAM -> 0xFF6D4C41
    }
    PieceColorTheme.PASTEL -> when (this) {
        PieceKind.LARGE_ONE -> 0xFFE99A9A
        PieceKind.LARGE_TWO -> 0xFFF2C28B
        PieceKind.MEDIUM -> 0xFF9CCDBD
        PieceKind.SMALL_ONE -> 0xFF9CB9D8
        PieceKind.SMALL_TWO -> 0xFFC1ADD6
        PieceKind.SQUARE -> 0xFFF1D98A
        PieceKind.PARALLELOGRAM -> 0xFFB89D8F
    }
    PieceColorTheme.OCEAN -> when (this) {
        PieceKind.LARGE_ONE -> 0xFF006D77
        PieceKind.LARGE_TWO -> 0xFF028090
        PieceKind.MEDIUM -> 0xFF00A896
        PieceKind.SMALL_ONE -> 0xFF2A9D8F
        PieceKind.SMALL_TWO -> 0xFF56CFE1
        PieceKind.SQUARE -> 0xFF83C5BE
        PieceKind.PARALLELOGRAM -> 0xFF264653
    }
    PieceColorTheme.SUNSET -> when (this) {
        PieceKind.LARGE_ONE -> 0xFFE76F51
        PieceKind.LARGE_TWO -> 0xFFF4A261
        PieceKind.MEDIUM -> 0xFFE9C46A
        PieceKind.SMALL_ONE -> 0xFFF28482
        PieceKind.SMALL_TWO -> 0xFFB56576
        PieceKind.SQUARE -> 0xFFFFC857
        PieceKind.PARALLELOGRAM -> 0xFF6D597A
    }
    PieceColorTheme.MONOCHROME -> when (this) {
        PieceKind.LARGE_ONE -> 0xFF263238
        PieceKind.LARGE_TWO -> 0xFF37474F
        PieceKind.MEDIUM -> 0xFF455A64
        PieceKind.SMALL_ONE -> 0xFF546E7A
        PieceKind.SMALL_TWO -> 0xFF607D8B
        PieceKind.SQUARE -> 0xFF78909C
        PieceKind.PARALLELOGRAM -> 0xFF90A4AE
    }
}

fun transformedVertices(piece: PlayingPiece, normalizedYScale: Float = 1f): List<Vec2> {
    val radians = Math.toRadians(piece.pose.rotation.toDouble())
    val c = cos(radians).toFloat()
    val s = sin(radians).toFloat()
    return piece.spec.vertices.map { source ->
        val x = if (piece.pose.flipped) -source.x else source.x
        Vec2(
            piece.pose.center.x + x * c - source.y * s,
            piece.pose.center.y + (x * s + source.y * c) * normalizedYScale
        )
    }
}

fun containsPoint(vertices: List<Vec2>, point: Vec2): Boolean {
    var inside = false
    var j = vertices.lastIndex
    vertices.indices.forEach { i ->
        val a = vertices[i]
        val b = vertices[j]
        if ((a.y > point.y) != (b.y > point.y) &&
            point.x < (b.x - a.x) * (point.y - a.y) / (b.y - a.y) + a.x
        ) inside = !inside
        j = i
    }
    return inside
}

fun containsPointWithTolerance(vertices: List<Vec2>, point: Vec2, tolerance: Float): Boolean {
    if (containsPoint(vertices, point)) return true
    return vertices.indices.any { index ->
        distanceToSegment(point, vertices[index], vertices[(index + 1) % vertices.size]) <= tolerance
    }
}

private fun distanceToSegment(point: Vec2, start: Vec2, end: Vec2): Float {
    val dx = end.x - start.x
    val dy = end.y - start.y
    val lengthSquared = dx * dx + dy * dy
    if (lengthSquared == 0f) {
        return kotlin.math.hypot(point.x - start.x, point.y - start.y)
    }
    val projection = ((point.x - start.x) * dx + (point.y - start.y) * dy) / lengthSquared
    val t = projection.coerceIn(0f, 1f)
    return kotlin.math.hypot(point.x - (start.x + t * dx), point.y - (start.y + t * dy))
}

fun angleDistance(a: Int, b: Int): Int {
    val difference = abs(((a - b) % 360 + 360) % 360)
    return minOf(difference, 360 - difference)
}

fun PieceKind.canUseTarget(target: PieceKind): Boolean = when (this) {
    PieceKind.LARGE_ONE, PieceKind.LARGE_TWO -> target == PieceKind.LARGE_ONE || target == PieceKind.LARGE_TWO
    PieceKind.SMALL_ONE, PieceKind.SMALL_TWO -> target == PieceKind.SMALL_ONE || target == PieceKind.SMALL_TWO
    else -> this == target
}

fun rotationMatches(kind: PieceKind, rotation: Int, targetRotation: Int): Boolean {
    val period = when (kind) {
        PieceKind.SQUARE -> 90
        PieceKind.PARALLELOGRAM -> 180
        else -> 360
    }
    val difference = ((rotation - targetRotation) % period + period) % period
    return difference == 0
}

fun orientationMatches(kind: PieceKind, pose: Pose, target: Pose): Boolean =
    rotationMatches(kind, pose.rotation, target.rotation) &&
        (kind != PieceKind.PARALLELOGRAM || pose.flipped == target.flipped)
