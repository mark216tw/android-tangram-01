package com.example.minitangram.game

private fun targets(
    centers: List<Vec2>,
    rotations: List<Int>,
    flippedParallelogram: Boolean = false
) = PieceKind.entries.mapIndexed { index, kind ->
    kind to Pose(centers[index], rotations[index], kind == PieceKind.PARALLELOGRAM && flippedParallelogram)
}.toMap()

val levels = listOf(
    Level(1, "小魚", targets(listOf(Vec2(.39f,.35f),Vec2(.55f,.35f),Vec2(.68f,.35f),Vec2(.76f,.29f),Vec2(.76f,.42f),Vec2(.51f,.49f),Vec2(.29f,.35f)), listOf(45,225,0,45,315,45,0), true)),
    Level(2, "小雞", targets(listOf(Vec2(.47f,.40f),Vec2(.58f,.45f),Vec2(.38f,.30f),Vec2(.30f,.23f),Vec2(.67f,.31f),Vec2(.52f,.59f),Vec2(.39f,.53f)), listOf(45,225,315,225,45,45,90))),
    Level(3, "兔子", targets(listOf(Vec2(.45f,.43f),Vec2(.58f,.43f),Vec2(.34f,.34f),Vec2(.31f,.20f),Vec2(.43f,.20f),Vec2(.64f,.54f),Vec2(.73f,.40f)), listOf(45,225,315,225,315,45,0), true)),
    Level(4, "貓咪", targets(listOf(Vec2(.43f,.43f),Vec2(.57f,.43f),Vec2(.68f,.29f),Vec2(.35f,.24f),Vec2(.48f,.23f),Vec2(.35f,.57f),Vec2(.67f,.55f)), listOf(45,225,315,225,315,45,90), true)),
    Level(5, "小狗", targets(listOf(Vec2(.44f,.42f),Vec2(.58f,.44f),Vec2(.68f,.34f),Vec2(.33f,.25f),Vec2(.73f,.24f),Vec2(.39f,.58f),Vec2(.67f,.57f)), listOf(45,225,315,225,45,45,90))),
    Level(6, "烏龜", targets(listOf(Vec2(.41f,.39f),Vec2(.57f,.39f),Vec2(.70f,.40f),Vec2(.31f,.52f),Vec2(.65f,.54f),Vec2(.49f,.55f),Vec2(.25f,.39f)), listOf(45,225,315,45,315,45,0), true)),
    Level(7, "天鵝", targets(listOf(Vec2(.43f,.48f),Vec2(.57f,.48f),Vec2(.57f,.31f),Vec2(.64f,.18f),Vec2(.73f,.18f),Vec2(.38f,.61f),Vec2(.62f,.39f)), listOf(45,225,315,45,315,45,90))),
    Level(8, "狐狸", targets(listOf(Vec2(.44f,.43f),Vec2(.57f,.44f),Vec2(.37f,.28f),Vec2(.46f,.20f),Vec2(.62f,.22f),Vec2(.66f,.57f),Vec2(.29f,.49f)), listOf(45,225,315,225,315,45,90), true)),
    Level(9, "駿馬", targets(listOf(Vec2(.45f,.39f),Vec2(.59f,.44f),Vec2(.67f,.27f),Vec2(.75f,.20f),Vec2(.34f,.56f),Vec2(.56f,.59f),Vec2(.30f,.35f)), listOf(45,225,315,45,315,45,90), true)),
    Level(10, "大象", targets(listOf(Vec2(.43f,.42f),Vec2(.58f,.42f),Vec2(.70f,.36f),Vec2(.77f,.50f),Vec2(.33f,.55f),Vec2(.55f,.59f),Vec2(.27f,.38f)), listOf(45,225,315,45,315,45,90))),
    Level(11, "老鷹", targets(listOf(Vec2(.36f,.39f),Vec2(.64f,.39f),Vec2(.50f,.47f),Vec2(.22f,.28f),Vec2(.78f,.28f),Vec2(.50f,.61f),Vec2(.50f,.29f)), listOf(45,315,45,225,315,45,0))),
    Level(12, "飛龍", targets(listOf(Vec2(.42f,.39f),Vec2(.59f,.34f),Vec2(.68f,.52f),Vec2(.78f,.44f),Vec2(.29f,.25f),Vec2(.48f,.56f),Vec2(.28f,.42f)), listOf(45,225,315,45,225,45,90), true))
)
