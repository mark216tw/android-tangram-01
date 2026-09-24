package com.example.minitangram.game

private fun targets(
    centers: List<Vec2>,
    rotations: List<Int>,
    flippedParallelogram: Boolean = false
) = PieceKind.entries.mapIndexed { index, kind ->
    kind to Pose(centers[index], rotations[index], kind == PieceKind.PARALLELOGRAM && flippedParallelogram)
}.toMap()

private fun level(id: Int, name: String, centers: List<Vec2>, rotations: List<Int>, flipped: Boolean = false) =
    Level(id, name, targets(centers, rotations, flipped))

// Coordinates are normalized to the board. Each arrangement follows the supplied
// reference sheet while retaining the standard seven-piece tangram geometry.
val levels = listOf(
    // Snake: a low zig-zag body with the head raised at the right.
    level(1, "蛇", listOf(Vec2(.30f,.48f),Vec2(.43f,.48f),Vec2(.57f,.40f),Vec2(.69f,.29f),Vec2(.76f,.39f),Vec2(.51f,.55f),Vec2(.28f,.34f)), listOf(0,180,315,45,225,45,315)),
    // Lion: the large triangles form the mane, with the small pieces as ears and muzzle.
    level(2, "獅子", listOf(Vec2(.39f,.45f),Vec2(.56f,.45f),Vec2(.69f,.40f),Vec2(.29f,.28f),Vec2(.68f,.25f),Vec2(.48f,.29f),Vec2(.30f,.53f)), listOf(45,225,0,225,45,45,90)),
    // Eagle: two extended wings, a pointed body and a raised head.
    level(3, "老鷹", listOf(Vec2(.34f,.40f),Vec2(.66f,.40f),Vec2(.50f,.51f),Vec2(.23f,.29f),Vec2(.77f,.29f),Vec2(.50f,.65f),Vec2(.50f,.27f)), listOf(45,315,45,225,315,45,0)),
    level(4, "蝴蝶", listOf(Vec2(.36f,.39f),Vec2(.64f,.39f),Vec2(.50f,.50f),Vec2(.28f,.28f),Vec2(.72f,.28f),Vec2(.50f,.29f),Vec2(.50f,.68f)), listOf(45,315,0,315,45,45,90)),
    level(5, "小雞", listOf(Vec2(.47f,.40f),Vec2(.58f,.45f),Vec2(.38f,.30f),Vec2(.30f,.23f),Vec2(.67f,.31f),Vec2(.52f,.59f),Vec2(.39f,.53f)), listOf(45,225,315,225,45,45,90)),
    level(6, "鴨子", listOf(Vec2(.42f,.43f),Vec2(.58f,.43f),Vec2(.72f,.36f),Vec2(.31f,.26f),Vec2(.75f,.23f),Vec2(.48f,.59f),Vec2(.35f,.55f)), listOf(45,225,0,225,45,45,315)),
    level(7, "駱駝", listOf(Vec2(.39f,.43f),Vec2(.58f,.43f),Vec2(.73f,.39f),Vec2(.31f,.22f),Vec2(.68f,.22f),Vec2(.48f,.58f),Vec2(.28f,.48f)), listOf(45,225,0,225,45,45,90)),
    level(8, "蘋果", listOf(Vec2(.43f,.48f),Vec2(.57f,.48f),Vec2(.50f,.64f),Vec2(.34f,.35f),Vec2(.66f,.35f),Vec2(.50f,.29f),Vec2(.50f,.19f)), listOf(45,225,0,315,45,45,0)),
    level(9, "長頸鹿", listOf(Vec2(.42f,.48f),Vec2(.58f,.48f),Vec2(.70f,.42f),Vec2(.35f,.24f),Vec2(.73f,.25f),Vec2(.48f,.60f),Vec2(.27f,.40f)), listOf(45,225,0,225,45,45,90)),
    level(10, "恐龍", listOf(Vec2(.42f,.43f),Vec2(.58f,.43f),Vec2(.72f,.35f),Vec2(.34f,.27f),Vec2(.70f,.23f),Vec2(.50f,.60f),Vec2(.27f,.51f)), listOf(45,225,315,225,45,45,90), true),
    level(11, "狐狸", listOf(Vec2(.44f,.43f),Vec2(.57f,.44f),Vec2(.37f,.28f),Vec2(.46f,.20f),Vec2(.62f,.22f),Vec2(.66f,.57f),Vec2(.29f,.49f)), listOf(45,225,315,225,315,45,90), true),
    level(12, "蝙蝠", listOf(Vec2(.34f,.40f),Vec2(.66f,.40f),Vec2(.50f,.49f),Vec2(.23f,.28f),Vec2(.77f,.28f),Vec2(.50f,.63f),Vec2(.50f,.28f)), listOf(315,45,0,225,315,45,90)),
    level(13, "愛心", listOf(Vec2(.39f,.39f),Vec2(.61f,.39f),Vec2(.50f,.55f),Vec2(.29f,.27f),Vec2(.71f,.27f),Vec2(.39f,.63f),Vec2(.61f,.63f)), listOf(45,225,0,315,45,315,45)),
    level(14, "橋", listOf(Vec2(.32f,.49f),Vec2(.68f,.49f),Vec2(.50f,.38f),Vec2(.27f,.26f),Vec2(.73f,.26f),Vec2(.50f,.58f),Vec2(.50f,.25f)), listOf(0,180,0,225,315,45,90)),
    level(15, "蠟燭", listOf(Vec2(.50f,.53f),Vec2(.50f,.68f),Vec2(.50f,.39f),Vec2(.50f,.23f),Vec2(.50f,.14f),Vec2(.39f,.53f),Vec2(.61f,.53f)), listOf(45,225,0,45,315,0,0)),
    level(16, "鑰匙", listOf(Vec2(.38f,.50f),Vec2(.54f,.50f),Vec2(.68f,.50f),Vec2(.72f,.31f),Vec2(.72f,.67f),Vec2(.30f,.32f),Vec2(.30f,.68f)), listOf(0,0,0,45,225,45,315)),
    level(17, "兔子", listOf(Vec2(.45f,.43f),Vec2(.58f,.43f),Vec2(.34f,.34f),Vec2(.31f,.20f),Vec2(.43f,.20f),Vec2(.64f,.54f),Vec2(.73f,.40f)), listOf(45,225,315,225,315,45,0), true),
    level(18, "袋鼠", listOf(Vec2(.42f,.44f),Vec2(.57f,.44f),Vec2(.70f,.42f),Vec2(.35f,.27f),Vec2(.73f,.25f),Vec2(.49f,.60f),Vec2(.28f,.51f)), listOf(45,225,0,225,45,45,90)),
    level(19, "蝦子", listOf(Vec2(.34f,.47f),Vec2(.48f,.36f),Vec2(.61f,.31f),Vec2(.72f,.38f),Vec2(.69f,.55f),Vec2(.49f,.56f),Vec2(.29f,.59f)), listOf(45,135,225,315,45,0,315)),
    level(20, "天鵝", listOf(Vec2(.43f,.48f),Vec2(.57f,.48f),Vec2(.57f,.31f),Vec2(.64f,.18f),Vec2(.73f,.18f),Vec2(.38f,.61f),Vec2(.62f,.39f)), listOf(45,225,315,45,315,45,90))
)
