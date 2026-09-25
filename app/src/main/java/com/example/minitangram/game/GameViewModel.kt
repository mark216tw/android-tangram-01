package com.example.minitangram.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.hypot

data class GameUiState(
    val pieces: List<PlayingPiece>,
    val selected: PieceKind? = null,
    val elapsedSeconds: Long = 0,
    val completed: Boolean = false,
    val hint: PieceKind? = null,
    val hintTarget: PieceKind? = null,
    val hintsUsed: Int = 0,
    val snapCount: Int = 0
)

class GameViewModel(val level: Level, initialYScale: Float = 1f) : ViewModel() {
    private var normalizedYScale = initialYScale.coerceAtLeast(.01f)
    private val _state = MutableStateFlow(newGame())
    val state: StateFlow<GameUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            while (isActive) {
                delay(1_000)
                _state.update { if (it.completed) it else it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
            }
        }
    }

    private fun newGame(): GameUiState {
        val initialPoses = initialPiecePoses(normalizedYScale)
        return GameUiState(pieceSpecs.map { spec ->
            PlayingPiece(spec, initialPoses.getValue(spec.kind))
        })
    }

    fun select(point: Vec2, normalizedYScale: Float = 1f) {
        this.normalizedYScale = normalizedYScale.coerceAtLeast(.01f)
        val kind = _state.value.pieces.asReversed()
            .firstOrNull { !it.snapped && containsPoint(transformedVertices(it, normalizedYScale), point) }?.spec?.kind
        _state.update { current ->
            if (kind == null) current.copy(selected = null)
            else current.copy(
                selected = kind,
                pieces = current.pieces.sortedBy { it.spec.kind == kind }
            )
        }
    }

    fun drag(delta: Vec2) {
        val selected = _state.value.selected ?: return
        changePiece(selected) { piece ->
            piece.copy(pose = piece.pose.copy(center = Vec2(
                (piece.pose.center.x + delta.x).coerceIn(.04f, .96f),
                (piece.pose.center.y + delta.y).coerceIn(.06f, .96f)
            )))
        }
    }

    fun finishGesture() = trySnap()

    fun rotate(amount: Int) {
        val selected = _state.value.selected ?: return
        changePiece(selected) { it.copy(pose = it.pose.copy(rotation = (it.pose.rotation + amount + 360) % 360)) }
        trySnap()
    }

    fun flip() {
        val selected = _state.value.selected ?: return
        if (selected != PieceKind.PARALLELOGRAM) return
        changePiece(selected) { it.copy(pose = it.pose.copy(flipped = !it.pose.flipped)) }
        // Re-evaluate immediately so a flipped piece can lock without another drag.
        trySnap()
    }

    fun hint() {
        val current = _state.value
        val piece = current.pieces.firstOrNull { !it.snapped } ?: return
        val occupiedTargets = current.pieces.mapNotNull { it.snappedTarget }.toSet()
        val targetKind = level.targets.keys.firstOrNull {
            it !in occupiedTargets && piece.spec.kind.canUseTarget(it)
        } ?: return
        val kind = piece.spec.kind
        _state.update { it.copy(hint = kind, hintTarget = targetKind, hintsUsed = it.hintsUsed + 1) }
        viewModelScope.launch {
            delay(1_600)
            _state.update {
                if (it.hint == kind && it.hintTarget == targetKind) it.copy(hint = null, hintTarget = null) else it
            }
        }
    }

    fun reset() { _state.value = newGame() }

    private fun trySnap() {
        val selected = _state.value.selected ?: return
        val current = _state.value
        val piece = current.pieces.first { it.spec.kind == selected }
        val occupiedTargets = current.pieces.mapNotNull { it.snappedTarget }.toSet()
        val match = level.targets
            .asSequence()
            .filter { (kind, target) ->
                    kind !in occupiedTargets &&
                    selected.canUseTarget(kind) &&
                    orientationMatches(selected, piece.pose, target)
            }
            .map { (kind, target) ->
                val distance = hypot(
                    (piece.pose.center.x - target.center.x).toDouble(),
                    ((piece.pose.center.y - target.center.y) / normalizedYScale).toDouble()
                )
                Triple(kind, target, distance)
            }
            // Keep the tolerance tight enough that a nearby square cannot
            // steal a triangular target (or vice versa).
            .filter { it.third <= if (selected == PieceKind.SQUARE) .052 else .065 }
            .minByOrNull { it.third }

        if (match != null) {
            val (targetKind, target) = match
            changePiece(selected) { it.copy(pose = target, snapped = true, snappedTarget = targetKind) }
            _state.update { current ->
                val done = current.pieces.all { it.snapped }
                current.copy(
                    selected = null,
                    completed = done,
                    hint = null,
                    hintTarget = null,
                    snapCount = current.snapCount + 1
                )
            }
        }
    }

    private fun changePiece(kind: PieceKind, transform: (PlayingPiece) -> PlayingPiece) {
        _state.update { current ->
            current.copy(pieces = current.pieces.map { if (it.spec.kind == kind) transform(it) else it })
        }
    }
}
