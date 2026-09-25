package com.example.minitangram.game

import kotlin.math.abs
import kotlin.math.hypot

/** Translation-only edge and vertex snapping in width-relative, isotropic canvas coordinates. */
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
    data class EdgeCandidate(val delta: Vec2, val source: PieceKind)
    val edgeCandidates = mutableListOf<EdgeCandidate>()
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
            // Keep the perpendicular correction and each nearby endpoint correction as
            // separate candidates so two independent edge corrections can be combined.
            edgeCandidates += EdgeCandidate(perpendicular, otherKind)
            listOf(-start, -end, otherLength - start, otherLength - end)
                .filter { abs(it) <= threshold }
                .forEach { edgeCandidates += EdgeCandidate(perpendicular + times(tangent, it), otherKind) }
        }
    }

    val candidates = (edgeCandidates.map { it.delta } + edgeCandidates.flatMapIndexed { index, first ->
        edgeCandidates.drop(index + 1)
            .filter { it.source != first.source }
            .filter {
                val firstLength = hypot(first.delta.x, first.delta.y)
                val secondLength = hypot(it.delta.x, it.delta.y)
                firstLength <= .00001f || secondLength <= .00001f ||
                    abs(dot(first.delta, it.delta)) <= firstLength * secondLength * .2f
            }
            .map { second -> first.delta + second.delta }
    } + buildList {
        for (ownVertex in own) for ((otherKind, otherPose) in others) {
            for (otherVertex in vertices(otherKind, otherPose)) {
                val delta = otherVertex - ownVertex
                if (hypot(delta.x, delta.y) <= threshold) add(delta)
            }
        }
    }).filter { hypot(it.x, it.y) <= threshold * 1.5f }

    fun contactCounts(delta: Vec2): Pair<Int, Int> {
        val moved = own.map { it + delta }
        var edgeContacts = 0
        var vertexContacts = 0
        for ((otherKind, otherPose) in others) {
            val other = vertices(otherKind, otherPose)
            for (i in moved.indices) {
                val a = moved[i]
                val b = moved[(i + 1) % moved.size]
                val ab = b - a
                val length = hypot(ab.x, ab.y)
                if (length <= .00001f) continue
                val tangent = times(ab, 1f / length)
                val normal = Vec2(-tangent.y, tangent.x)
                for (j in other.indices) {
                    val c = other[j]
                    val d = other[(j + 1) % other.size]
                    val cd = d - c
                    val otherLength = hypot(cd.x, cd.y)
                    if (otherLength <= .00001f) continue
                    if (abs(dot(cd, normal)) > .0001f * otherLength) continue
                    if (abs(dot(c - a, normal)) > .0001f) continue
                    val start = maxOf(dot(a - c, tangent), 0f)
                    val end = minOf(dot(b - c, tangent), otherLength)
                    if (end - start > .00001f) edgeContacts++
                }
            }
            moved.forEach { ownVertex ->
                if (other.any { otherVertex -> hypot(ownVertex.x - otherVertex.x, ownVertex.y - otherVertex.y) <= .0001f }) {
                    vertexContacts++
                }
            }
        }
        return edgeContacts to vertexContacts
    }

    val best = candidates.maxWithOrNull(
        compareBy<Vec2> { contactCounts(it).first }
            .thenByDescending { -hypot(it.x, it.y) }
            .thenBy { contactCounts(it).second }
    )
    return best?.let { pose.copy(center = pose.center + Vec2(it.x, it.y * yScale)) } ?: pose
}

fun tidyEditorPoses(poses: Map<PieceKind, Pose>, yScale: Float, threshold: Float): Map<PieceKind, Pose> {
    val result = linkedMapOf<PieceKind, Pose>()
    poses.forEach { (kind, pose) -> result[kind] = snapEditorPose(kind, pose, result, yScale, threshold) }
    return result
}
