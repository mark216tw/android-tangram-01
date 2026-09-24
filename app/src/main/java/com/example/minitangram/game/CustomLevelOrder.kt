package com.example.minitangram.game

/** Updating a saved level must not change its position in the user's list. */
fun List<Level>.saveInPlace(level: Level): List<Level> =
    if (any { it.id == level.id }) map { if (it.id == level.id) level else it }
    else this + level
