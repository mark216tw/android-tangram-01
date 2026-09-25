package com.example.minitangram.game

private data class ModelPose(
    val kind: PieceKind,
    val x: Float,
    val y: Float,
    val rotation: Int,
    val flipped: Boolean = false
)

private fun p(kind: PieceKind, x: Double, y: Double, rotation: Int, flipped: Boolean = false) =
    ModelPose(kind, x.toFloat(), y.toFloat(), rotation, flipped)

private fun level(id: Int, name: String, canvasYScale: Float, vararg poses: ModelPose): Level {
    require(poses.size == PieceKind.entries.size)
    require(poses.map { it.kind }.toSet() == PieceKind.entries.toSet())
    val targets = poses.associate { pose ->
        pose.kind to Pose(Vec2(pose.x, pose.y), pose.rotation, pose.flipped)
    }
    return Level(id, name, targets, canvasYScale)
}

val levels = listOf(
    level(1, "蛇", 0.5913853f, p(PieceKind.LARGE_ONE, 0.29061943, 0.430668, 0), p(PieceKind.LARGE_TWO, 0.5411827, 0.40076557, 180), p(PieceKind.MEDIUM, 0.5411827, 0.28248852, 135), p(PieceKind.SMALL_ONE, 0.69724864, 0.10148564, 180), p(PieceKind.SMALL_TWO, 0.7033146, 0.15196352, 225), p(PieceKind.SQUARE, 0.5972486, 0.19378078, 225), p(PieceKind.PARALLELOGRAM, 0.2656194, 0.54894507, 135, true)),
    level(2, "老鷹", 0.5913853f, p(PieceKind.LARGE_ONE, 0.2858085, 0.39443782, 0), p(PieceKind.LARGE_TWO, 0.58580834, 0.21702217, 0), p(PieceKind.MEDIUM, 0.67277014, 0.43848196, 0), p(PieceKind.SMALL_ONE, 0.28580853, 0.21993253, 180), p(PieceKind.SMALL_TWO, 0.38580853, 0.30573004, 270), p(PieceKind.SQUARE, 0.5414373, 0.36512703, 315), p(PieceKind.PARALLELOGRAM, 0.41080853, 0.24659139, 45)),
    level(3, "蝴蝶", 0.5913853f, p(PieceKind.LARGE_ONE, 0.3221411, 0.2760077, 135), p(PieceKind.LARGE_TWO, 0.71356267, 0.37189025, 0), p(PieceKind.MEDIUM, 0.56356263, 0.4888576, 315), p(PieceKind.SMALL_ONE, 0.42820707, 0.33873364, 315), p(PieceKind.SMALL_TWO, 0.25143057, 0.44327682, 45), p(PieceKind.SQUARE, 0.3574964, 0.40145954, 315), p(PieceKind.PARALLELOGRAM, 0.53856266, 0.40145952, 315, true)),
    level(4, "恐龍", 0.5913853f, p(PieceKind.LARGE_ONE, 0.5444545, 0.34346128, 315), p(PieceKind.LARGE_TWO, 0.71516496, 0.20260008, 0), p(PieceKind.MEDIUM, 0.58809453, 0.4947212, 270), p(PieceKind.SMALL_ONE, 0.6651652, 0.37310398, 0), p(PieceKind.SMALL_TWO, 0.77880967, 0.26591724, 45), p(PieceKind.SQUARE, 0.40303308, 0.40618715, 315), p(PieceKind.PARALLELOGRAM, 0.24393408, 0.43755007, 0)),
    level(5, "狐狸", 0.5913853f, p(PieceKind.LARGE_ONE, 0.34493327, 0.46705616, 0), p(PieceKind.LARGE_TWO, 0.55877095, 0.36610037, 45), p(PieceKind.MEDIUM, 0.70877093, 0.31203514, 225), p(PieceKind.SMALL_ONE, 0.17658442, 0.27795076, 135), p(PieceKind.SMALL_TWO, 0.31800586, 0.27795073, 315), p(PieceKind.SQUARE, 0.24729505, 0.3406764, 315), p(PieceKind.PARALLELOGRAM, 0.6141851, 0.1887556, 270)),
    level(6, "蝙蝠", 0.5913853f, p(PieceKind.LARGE_ONE, 0.30000007, 0.39913854, 0), p(PieceKind.LARGE_TWO, 0.5999999, 0.22172299, 0), p(PieceKind.MEDIUM, 0.59322935, 0.32668284, 180), p(PieceKind.SMALL_ONE, 0.30000007, 0.28086153, 90), p(PieceKind.SMALL_TWO, 0.40000015, 0.22172296, 270), p(PieceKind.SQUARE, 0.42500013, 0.29564613, 0), p(PieceKind.PARALLELOGRAM, 0.5049394, 0.39980483, 180, true)),
    level(7, "天鵝", 0.5913853f, p(PieceKind.LARGE_ONE, 0.6596252, 0.41992152, 225), p(PieceKind.LARGE_TWO, 0.54749316, 0.49638134, 270), p(PieceKind.MEDIUM, 0.39749318, 0.4668121, 315), p(PieceKind.SMALL_ONE, 0.35451606, 0.21175542, 180), p(PieceKind.SMALL_TWO, 0.33380544, 0.40352026, 135), p(PieceKind.SQUARE, 0.40451607, 0.34079438, 45), p(PieceKind.PARALLELOGRAM, 0.4575491, 0.24670562, 90, true)),
    level(8, "蝦子", 0.5913853f, p(PieceKind.LARGE_ONE, 0.44090092, 0.24411748, 270), p(PieceKind.LARGE_TWO, 0.553033, 0.34507328, 225), p(PieceKind.MEDIUM, 0.41161156, 0.51234233, 270), p(PieceKind.SMALL_ONE, 0.6090989, 0.4618644, 180), p(PieceKind.SMALL_TWO, 0.55303293, 0.51234233, 225), p(PieceKind.SQUARE, 0.34090093, 0.3659819, 315), p(PieceKind.PARALLELOGRAM, 0.28786793, 0.46007073, 90, true)),
    level(9, "北極熊", 0.5913853f, p(PieceKind.LARGE_ONE, 0.76464474, 0.3187515, 135), p(PieceKind.LARGE_TWO, 0.59393406, 0.25243828, 90), p(PieceKind.MEDIUM, 0.46464452, 0.27693424, 270), p(PieceKind.SMALL_ONE, 0.72207403, 0.45713103, 180), p(PieceKind.SMALL_TWO, 0.1939339, 0.25243828, 180), p(PieceKind.SQUARE, 0.3189339, 0.23765364, 0), p(PieceKind.PARALLELOGRAM, 0.39393383, 0.36310536, 315)),
    level(10, "趴著的貓", 0.5913853f, p(PieceKind.LARGE_ONE, 0.5749998, 0.44200653, 45), p(PieceKind.LARGE_TWO, 0.21286811, 0.40526256, 45), p(PieceKind.MEDIUM, 0.4249998, 0.38794127, 225), p(PieceKind.SMALL_ONE, 0.03609162, 0.25890207, 135), p(PieceKind.SMALL_TWO, 0.17751275, 0.25890213, 315), p(PieceKind.SQUARE, 0.10751391, 0.3212071, 45), p(PieceKind.PARALLELOGRAM, 0.84016484, 0.45246086, 180)),
    level(11, "馬", 0.5913853f, p(PieceKind.LARGE_ONE, 0.44640175, 0.33103162, 90), p(PieceKind.LARGE_TWO, 0.612177, 0.39734486, 135), p(PieceKind.MEDIUM, 0.39640176, 0.15361603, 45), p(PieceKind.SMALL_ONE, 0.2464017, 0.31371033, 45), p(PieceKind.SMALL_TWO, 0.50611097, 0.5046358, 315), p(PieceKind.SQUARE, 0.47140175, 0.2275392, 0), p(PieceKind.PARALLELOGRAM, 0.8066313, 0.49143368, 90, true)),
    level(12, "狐狸", 0.5913853f, p(PieceKind.LARGE_ONE, 0.825, 0.28202882, 270), p(PieceKind.LARGE_TWO, 0.62500006, 0.33579147, 90), p(PieceKind.MEDIUM, 0.4250001, 0.33579147, 45), p(PieceKind.SMALL_ONE, 0.32500002, 0.39493, 0), p(PieceKind.SMALL_TWO, 0.8569189, 0.19994955, 315), p(PieceKind.SQUARE, 0.05, 0.49842244, 0), p(PieceKind.PARALLELOGRAM, 0.125, 0.40971464, 135)),
    level(13, "鯊魚", 0.5913853f, p(PieceKind.LARGE_ONE, 0.2310661, 0.38299078, 45), p(PieceKind.LARGE_TWO, 0.4310661, 0.3584948, 90), p(PieceKind.MEDIUM, 0.29319814, 0.45437732, 225), p(PieceKind.SMALL_ONE, 0.511184, 0.26978695, 270), p(PieceKind.SMALL_TWO, 0.88106596, 0.22562271, 270), p(PieceKind.SQUARE, 0.6060661, 0.34371012, 0), p(PieceKind.PARALLELOGRAM, 0.7560659, 0.29935625, 135, true)),
    level(14, "鴨子", 0.5913853f, p(PieceKind.LARGE_ONE, 0.375, 0.35402793, 0), p(PieceKind.LARGE_TWO, 0.475, 0.41316646, 180), p(PieceKind.MEDIUM, 0.22500001, 0.38511065, 315), p(PieceKind.SMALL_ONE, 0.62499994, 0.32410938, 0), p(PieceKind.SMALL_TWO, 0.775, 0.26532012, 270), p(PieceKind.SQUARE, 0.65, 0.2505355, 0), p(PieceKind.PARALLELOGRAM, 0.65, 0.3832479, 135, true)),
    level(15, "狗", 0.5913853f, p(PieceKind.LARGE_ONE, 0.34448963, 0.38446787, 0), p(PieceKind.LARGE_TWO, 0.546731, 0.38314232, 180), p(PieceKind.MEDIUM, 0.79673105, 0.235296, 45), p(PieceKind.SMALL_ONE, 0.69673103, 0.38197812, 0), p(PieceKind.SMALL_TWO, 0.7174417, 0.4411167, 315), p(PieceKind.SQUARE, 0.72173107, 0.30921915, 0), p(PieceKind.PARALLELOGRAM, 0.20326892, 0.2809754, 45, true)),
    level(16, "鯨魚", 0.5913853f, p(PieceKind.LARGE_ONE, 0.27295133, 0.3595834, 90), p(PieceKind.LARGE_TWO, 0.47295132, 0.41872203, 270), p(PieceKind.MEDIUM, 0.77704865, 0.23170863, 225), p(PieceKind.SMALL_ONE, 0.7416933, 0.35357305, 315), p(PieceKind.SMALL_TWO, 0.5649167, 0.37448168, 225), p(PieceKind.SQUARE, 0.6729513, 0.41513473, 315), p(PieceKind.PARALLELOGRAM, 0.22295135, 0.43350667, 45, true)),
    level(17, "螃蟹", 0.5913853f, p(PieceKind.LARGE_ONE, 0.37886825, 0.37185687, 270), p(PieceKind.LARGE_TWO, 0.62113166, 0.3968509, 90), p(PieceKind.MEDIUM, 0.22886826, 0.25907618, 315), p(PieceKind.SMALL_ONE, 0.7711317, 0.21943529, 270), p(PieceKind.SMALL_TWO, 0.32886824, 0.4605647, 0), p(PieceKind.SQUARE, 0.5035302, 0.29335847, 270), p(PieceKind.PARALLELOGRAM, 0.7211317, 0.29335845, 315)),
    level(18, "烏龜", 0.5913853f, p(PieceKind.LARGE_ONE, 0.47388142, 0.2983865, 315), p(PieceKind.LARGE_TWO, 0.61530274, 0.29722252, 135), p(PieceKind.MEDIUM, 0.3719325, 0.44375435, 315), p(PieceKind.SMALL_ONE, 0.773606, 0.37384742, 225), p(PieceKind.SMALL_TWO, 0.7705149, 0.21785222, 45), p(PieceKind.SQUARE, 0.22639406, 0.2983865, 45), p(PieceKind.PARALLELOGRAM, 0.31990507, 0.17890084, 0)),
    level(19, "站立的貓", 0.5913853f, p(PieceKind.LARGE_ONE, 0.353859, 0.475906, 225), p(PieceKind.LARGE_TWO, 0.34999996, 0.39227155, 45), p(PieceKind.MEDIUM, 0.49142134, 0.5177233, 180), p(PieceKind.SMALL_ONE, 0.27739847, 0.1843035, 135), p(PieceKind.SMALL_TWO, 0.4207106, 0.18318532, 315), p(PieceKind.SQUARE, 0.34999996, 0.24591124, 45), p(PieceKind.PARALLELOGRAM, 0.71213204, 0.51518667, 135)),
    level(20, "牛", 0.5913853f, p(PieceKind.LARGE_ONE, 0.4335787, 0.40039766, 135), p(PieceKind.LARGE_TWO, 0.71642137, 0.40039766, 315), p(PieceKind.MEDIUM, 0.57500005, 0.34125912, 225), p(PieceKind.SMALL_ONE, 0.17751259, 0.2219319, 315), p(PieceKind.SMALL_TWO, 0.3982233, 0.21687649, 135), p(PieceKind.SQUARE, 0.28786793, 0.2811369, 0), p(PieceKind.PARALLELOGRAM, 0.8401651, 0.36903474, 270, true))
)

fun isBuiltInLevelUnlocked(id: Int, unlockedLevelId: Int): Boolean {
    val levelIndex = levels.indexOfFirst { it.id == id }
    val unlockedIndex = levels.indexOfFirst { it.id == unlockedLevelId }
    return levelIndex >= 0 && levelIndex <= unlockedIndex.coerceAtLeast(0)
}

fun nextBuiltInLevel(id: Int): Level? {
    val index = levels.indexOfFirst { it.id == id }
    return if (index >= 0) levels.getOrNull(index + 1) else null
}
