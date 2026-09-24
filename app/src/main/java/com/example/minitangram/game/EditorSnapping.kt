package com.example.minitangram.game

import kotlin.math.abs
import kotlin.math.hypot

/** Translation-only edge snapping in width-relative, isotropic canvas coordinates. */
fun snapEditorPose(
    kind: PieceKind,
    pose: Pose,
    others: Map<PieceKind, Pose>,
    yScale: Float,
    threshold: Float
): Pose {
    if (yScale <= 0f || threshold <= 0f) return pose
    fun vertices(k: PieceKind, p: Pose) = transformedVertices(
        PlayingPiece(pieceSpecs.first { it.kind == k }, p.copy(center = Vec2(p.center.x, p.center.y / yScale)))
    )
    fun dot(a: Vec2, b: Vec2) = a.x * b.x + a.y * b.y
    fun times(a: Vec2, s: Float) = Vec2(a.x * s, a.y * s)
    val own = vertices(kind, pose)
    val ownCenter = Vec2(pose.center.x, pose.center.y / yScale)
    var best: Vec2? = null
    var bestDistance = Float.POSITIVE_INFINITY
    for ((otherKind, otherPose) in others) {
        val other = vertices(otherKind, otherPose)
        val otherCenter = Vec2(otherPose.center.x, otherPose.center.y / yScale)
        for (i in own.indices) for (j in other.indices) {
            val a = own[i]
            val b = own[(i + 1) % own.size]
            val c = other[j]
            val d = other[(j + 1) % other.size]
            val ab = b - a
            val cd = d - c
            val length = hypot(ab.x, ab.y)
            val otherLength = hypot(cd.x, cd.y)
            val tangent = times(cd, 1f / otherLength)
            val normal = Vec2(-tangent.y, tangent.x)
            // Rotations are already 45-degree steps; never rotate to force a match.
            if (abs(dot(ab, normal)) > length * .0001f) continue
            val gap = dot(c - a, normal)
            if (abs(gap) > threshold) continue
            // Require actual segment overlap, not just nearby corners/extended lines.
            val start = minOf(dot(a - c, tangent), dot(b - c, tangent))
            val end = maxOf(dot(a - c, tangent), dot(b - c, tangent))
            if (minOf(end, otherLength) - maxOf(start, 0f) <= .00001f) continue
            val perpendicular = times(normal, gap)
            if (dot(ownCenter + perpendicular - c, normal) * dot(otherCenter - c, normal) >= 0f) continue
            // Endpoint alignment is only considered after an eligible edge match.
            val endpointShift = listOf(-start, -end, otherLength - start, otherLength - end)
                .filter { abs(it) <= threshold }.minByOrNull { abs(it) } ?: 0f
            val delta = perpendicular + times(tangent, endpointShift)
            val distance = hypot(delta.x, delta.y)
            if (distance < bestDistance) {
                bestDistance = distance
                best = delta
            }
        }
    }
    return best?.let { pose.copy(center = pose.center + Vec2(it.x, it.y * yScale)) } ?: pose
}

fun tidyEditorPoses(poses: Map<PieceKind, Pose>, yScale: Float, threshold: Float): Map<PieceKind, Pose> {
    val result = linkedMapOf<PieceKind, Pose>()
    poses.forEach { (kind, pose) -> result[kind] = snapEditorPose(kind, pose, result, yScale, threshold) }
    return result
}
