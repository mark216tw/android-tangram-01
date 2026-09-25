package com.example.minitangram

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import android.graphics.Canvas as AndroidCanvas
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import com.example.minitangram.game.forCanvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.RotateLeft
import androidx.compose.material.icons.automirrored.rounded.RotateRight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Flip
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.minitangram.data.PreferencesRepository
import com.example.minitangram.data.UserProgress
import com.example.minitangram.game.GameViewModel
import com.example.minitangram.game.Difficulty
import com.example.minitangram.game.Level
import com.example.minitangram.game.PieceKind
import com.example.minitangram.game.PieceColorTheme
import com.example.minitangram.game.PlayingPiece
import com.example.minitangram.game.Pose
import com.example.minitangram.game.Vec2
import com.example.minitangram.game.levels
import com.example.minitangram.game.pieceSpecs
import com.example.minitangram.game.containsPoint
import com.example.minitangram.game.colorFor
import com.example.minitangram.game.transformedVertices
import com.example.minitangram.game.TARGET_AREA_BOTTOM
import com.example.minitangram.ui.theme.DisplayMode
import com.example.minitangram.ui.theme.MiniTangramTheme
import kotlinx.coroutines.launch
import com.example.minitangram.game.snapEditorPose
import com.example.minitangram.game.tidyEditorPoses
import com.example.minitangram.game.centerEditorPoses
import com.example.minitangram.game.initialPiecePoses
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import java.io.File
import java.io.FileOutputStream

@Composable
fun MiniTangramApp(onExit: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { PreferencesRepository(context.applicationContext) }
    val progress by repository.progress.collectAsStateWithLifecycle(UserProgress())
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val allLevels = levels + progress.customLevels

    MiniTangramTheme(progress.displayMode) {
        NavHost(navController, startDestination = "home") {
            composable("home") {
                BackHandler(onBack = onExit)
                HomeScreen(
                    onStart = { navController.navigate("levels") },
                    onInstructions = { navController.navigate("instructions") },
                    onSettings = { navController.navigate("settings") },
                    onExit = onExit
                )
            }
            composable("levels") {
                LevelSelectScreen(navController, progress, repository, scope)
            }
            composable("editor/{levelId}", arguments = listOf(navArgument("levelId") { type = NavType.IntType })) { entry ->
                val id = entry.arguments?.getInt("levelId") ?: 0
                LevelEditorScreen(navController, repository, progress.customLevels.firstOrNull { it.id == id }, scope, progress.pieceColorTheme)
            }
            composable("instructions") {
                InstructionsScreen(navController)
            }
            composable("settings") {
                SettingsScreen(
                    navController = navController,
                    currentMode = progress.displayMode,
                    soundEnabled = progress.soundEnabled,
                    onModeChange = { mode -> scope.launch { repository.setDisplayMode(mode) } },
                    onSoundEnabledChange = { enabled -> scope.launch { repository.setSoundEnabled(enabled) } },
                    hideBuiltInLevels = progress.hideBuiltInLevels,
                    onHideBuiltInLevelsChange = { hidden -> scope.launch { repository.setHideBuiltInLevels(hidden) } },
                    difficulty = progress.difficulty,
                    onDifficultyChange = { difficulty -> scope.launch { repository.setDifficulty(difficulty) } },
                    colorTheme = progress.pieceColorTheme,
                    onColorThemeChange = { theme -> scope.launch { repository.setPieceColorTheme(theme) } }
                )
            }
            composable(
                route = "game/{levelId}",
                arguments = listOf(navArgument("levelId") { type = NavType.IntType })
            ) { entry ->
                val levelId = entry.arguments?.getInt("levelId") ?: 1
                val level = allLevels.first { it.id == levelId }
                GameScreen(
                    navController = navController,
                    level = level,
                    bestTime = progress.bestTimes[levelId],
                    soundEnabled = progress.soundEnabled,
                    onComplete = { seconds -> repository.completeLevel(levelId, seconds) },
                    difficulty = progress.difficulty,
                    isCustom = level.id < 0,
                    colorTheme = progress.pieceColorTheme
                )
            }
        }
    }
}

@Composable
private fun HomeScreen(
    onStart: () -> Unit,
    onInstructions: () -> Unit,
    onSettings: () -> Unit,
    onExit: () -> Unit
) {
    PaperBackground {
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(.35f))
            LauncherTangram(Modifier.size(154.dp))
            Spacer(Modifier.height(14.dp))
            Text("mini", color = MaterialTheme.colorScheme.primary, fontSize = 18.sp, letterSpacing = 6.sp)
            Text("七巧板", color = MaterialTheme.colorScheme.onBackground, fontSize = 42.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 8.sp)
            Text("七片巧思 · 萬般形意", color = MaterialTheme.colorScheme.outline, fontSize = 14.sp)
            Spacer(Modifier.weight(.20f))
            HomePrimaryCard(onStart)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HomeSmallCard(
                    title = "遊戲說明",
                    subtitle = "玩法與操作",
                    icon = { Icon(Icons.Rounded.Info, null) },
                    onClick = onInstructions,
                    modifier = Modifier.weight(1f)
                )
                HomeSmallCard(
                    title = "設定",
                    subtitle = "顯示與音效",
                    icon = { Icon(Icons.Rounded.Settings, null) },
                    onClick = onSettings,
                    modifier = Modifier.weight(1f)
                )
            }
            TextButton(onClick = onExit, modifier = Modifier.padding(top = 4.dp).height(46.dp)) { Text("結束遊戲") }
            Spacer(Modifier.weight(.62f))
        }
    }
}

@Composable
private fun HomePrimaryCard(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(78.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp, pressedElevation = 1.dp)
    ) {
        Row(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).background(MaterialTheme.colorScheme.onPrimary.copy(alpha = .16f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.PlayArrow, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("開始遊戲", color = MaterialTheme.colorScheme.onPrimary, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                Text("自訂關卡一起挑戰", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .78f), fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun HomeSmallCard(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(92.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, pressedElevation = 0.dp)
    ) {
        Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Box(Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                CompositionLocalProvider(androidx.compose.material3.LocalContentColor provides MaterialTheme.colorScheme.primary) {
                    icon()
                }
            }
            Column {
                Text(title, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = MaterialTheme.colorScheme.outline, fontSize = 11.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PageScaffold(title: String, navController: NavController, status: String? = null, content: @Composable (PaddingValues) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.SemiBold) },
                actions = {
                    status?.let { Text(it, modifier = Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.primary) }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        content = content
    )
}

@Composable
private fun LevelSelectScreen(
    navController: NavController,
    progress: UserProgress,
    repository: PreferencesRepository,
    scope: kotlinx.coroutines.CoroutineScope
) {
    val context = LocalContext.current
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri: Uri? ->
        if (uri != null) scope.launch {
            context.contentResolver.openOutputStream(uri)?.use { output ->
                output.write(repository.exportCustomLevels(progress.customLevels).toByteArray())
            }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) scope.launch {
            val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            if (json != null) repository.importCustomLevels(json)
        }
    }
    PageScaffold("遊戲關卡", navController, if (progress.difficulty == Difficulty.BEGINNER) "初級" else "高級") { padding ->
        val allLevels = if (progress.hideBuiltInLevels) progress.customLevels else levels + progress.customLevels
        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(padding)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(onClick = { navController.navigate("editor/0") }) { Icon(Icons.Rounded.Add, null); Text("建立") }
            FilledTonalButton(onClick = { importLauncher.launch(arrayOf("application/json", "text/plain")) }) { Icon(Icons.Rounded.FileUpload, null); Text("匯入") }
            FilledTonalButton(onClick = { exportLauncher.launch("mini-tangram-levels.json") }) { Icon(Icons.Rounded.FileDownload, null); Text("匯出") }
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(145.dp),
            contentPadding = PaddingValues(
                start = 18.dp,
                end = 18.dp,
                top = 4.dp,
                bottom = 24.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
        ) {
            items(allLevels) { level ->
                val unlocked = level.id < 0 || level.id <= progress.unlockedLevel
                val best = progress.bestTimes[level.id]
                Card(
                    modifier = Modifier.height(132.dp).clickable(enabled = unlocked) { navController.navigate("game/${level.id}") },
                    shape = RoundedCornerShape(6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            level.id < 0 -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = .55f)
                            unlocked -> MaterialTheme.colorScheme.surface
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Box(Modifier.fillMaxSize()) {
                    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(level.id.toString().padStart(2, '0'), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            if (level.id < 0) {
                                IconButton(onClick = { navController.navigate("editor/${level.id}") }, modifier = Modifier.size(28.dp)) { Icon(Icons.Rounded.Edit, "編輯") }
                            } else {
                                Icon(if (unlocked) Icons.Rounded.Check else Icons.Rounded.Lock, if (unlocked) "已解鎖" else "未解鎖", tint = if (best != null) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline)
                            }
                        }
                        Text(level.name, fontSize = 23.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        Text(best?.let { "已完成 · 最佳 ${formatTime(it)}" } ?: if (unlocked) "尚未完成" else "完成前一關解鎖", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun InstructionsScreen(navController: NavController) {
    PageScaffold("遊戲說明", navController) { padding ->
        Column(
            Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState())
                .padding(padding).padding(24.dp)
        ) {
            Text("以七片，成萬物", fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
            Text("將全部拼片放進上方剪影，拼成關卡指定圖案。", modifier = Modifier.padding(vertical = 16.dp), lineHeight = 25.sp)
            Instruction("壹 · 移動", "按住彩色拼片拖曳到想要的位置。被選取的拼片會浮到最上層。")
            Instruction("貳 · 旋轉", "選取拼片後，使用底部左右旋轉按鈕，每次轉動 45 度。")
            Instruction("參 · 翻面", "棕色平行四邊形可使用翻面按鈕切換方向。")
             Instruction("肆 · 吸附", "位置、角度及方向接近正確時，拼片會自動吸附並鎖定。")
             Instruction("伍 · 提示", "提示會短暫標示一塊尚未完成的拼片與它的目標位置。")
             Instruction("陸 · 自訂關卡", "在關卡頁可建立、編輯及刪除自訂關卡，也能匯入或匯出關卡檔案。建立時可拖曳拼片、旋轉、翻面，或用兩指移動整個圖案；排列完成後輸入名稱儲存即可遊玩。")
             HorizontalDivider(Modifier.padding(vertical = 20.dp))
            Text("完成關卡會記錄最佳時間並解鎖下一幅圖案。遊戲不需要網路連線。", color = MaterialTheme.colorScheme.outline, lineHeight = 23.sp)
        }
    }
}

@Composable
private fun Instruction(title: String, body: String) {
    Text(title, color = MaterialTheme.colorScheme.primary, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp, bottom = 5.dp))
    Text(body, lineHeight = 23.sp)
}

@Composable
private fun SettingsScreen(
    navController: NavController,
    currentMode: DisplayMode,
    soundEnabled: Boolean,
    onModeChange: (DisplayMode) -> Unit,
    onSoundEnabledChange: (Boolean) -> Unit,
    hideBuiltInLevels: Boolean,
    onHideBuiltInLevelsChange: (Boolean) -> Unit,
    difficulty: Difficulty,
    onDifficultyChange: (Difficulty) -> Unit,
    colorTheme: PieceColorTheme,
    onColorThemeChange: (PieceColorTheme) -> Unit
) {
    PageScaffold("設定", navController) { padding ->
        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp)) {
            Text("顯示模式", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DisplayMode.entries.forEach { mode ->
                val label = when (mode) { DisplayMode.SYSTEM -> "系統"; DisplayMode.LIGHT -> "淺色"; DisplayMode.DARK -> "深色" }
                SettingChoice(label, currentMode == mode, { onModeChange(mode) }, Modifier.weight(1f))
            }
            }
            HorizontalDivider(Modifier.padding(vertical = 10.dp))
            Text("遊戲難度", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Difficulty.entries.forEach { mode ->
                val label = if (mode == Difficulty.BEGINNER) "初級" else "高級"
                SettingChoice(label, difficulty == mode, { onDifficultyChange(mode) }, Modifier.weight(1f))
            }
            }
            Text(if (difficulty == Difficulty.BEGINNER) "顯示每塊拼板的完整外框" else "只顯示圖案的外圍輪廓", fontSize = 13.sp, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(top = 4.dp))
            HorizontalDivider(Modifier.padding(vertical = 10.dp))
            Text("拼板顏色", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            PieceColorTheme.entries.chunked(2).forEach { themes ->
            Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            themes.forEach { theme ->
                val label = when (theme) {
                    PieceColorTheme.CLASSIC -> "經典"
                    PieceColorTheme.BRIGHT -> "高彩"
                    PieceColorTheme.PASTEL -> "柔和"
                    PieceColorTheme.OCEAN -> "海洋"
                    PieceColorTheme.SUNSET -> "夕照"
                    PieceColorTheme.MONOCHROME -> "單色"
                }
                SettingChoice(label, colorTheme == theme, { onColorThemeChange(theme) }, Modifier.weight(1f)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        PieceKind.entries.forEach { kind ->
                            Box(Modifier.weight(1f).height(10.dp).background(Color(kind.colorFor(theme)), RoundedCornerShape(2.dp)))
                        }
                    }
                }
            }
            }
            }
            HorizontalDivider(Modifier.padding(vertical = 10.dp))
            Row(
                Modifier.fillMaxWidth().clickable { onSoundEnabledChange(!soundEnabled) }.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("音效", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                Switch(checked = soundEnabled, onCheckedChange = onSoundEnabledChange)
            }
            HorizontalDivider(Modifier.padding(vertical = 10.dp))
            Row(
                Modifier.fillMaxWidth().clickable { onHideBuiltInLevelsChange(!hideBuiltInLevels) }.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("隱藏內建拼圖", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                Switch(checked = hideBuiltInLevels, onCheckedChange = onHideBuiltInLevelsChange)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GameScreen(
    navController: NavController,
    level: Level,
    bestTime: Long?,
    soundEnabled: Boolean,
    onComplete: suspend (Long) -> Unit,
    difficulty: Difficulty,
    isCustom: Boolean,
    colorTheme: PieceColorTheme
) {
    val context = LocalContext.current
    var boardSize by remember { mutableStateOf(IntSize.Zero) }
    val boardYScale = if (boardSize.height > 0) boardSize.width.toFloat() / boardSize.height else 1f
    val canvasLevel = if (boardSize.height > 0) level.forCanvas(boardYScale) else level
    val displayLevel = if (level.id > 0) canvasLevel.copy(targets = centerEditorPoses(canvasLevel.targets, boardYScale)) else canvasLevel
    val factory = remember(displayLevel, boardYScale) {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = GameViewModel(displayLevel, boardYScale) as T
        }
    }
    val game: GameViewModel = viewModel(key = "game-${level.id}-${boardSize.width}-${boardSize.height}", factory = factory)
    val state by game.state.collectAsStateWithLifecycle()
    val toneGenerator = remember { ToneGenerator(AudioManager.STREAM_MUSIC, 70) }
    DisposableEffect(toneGenerator) {
        onDispose { toneGenerator.release() }
    }
    LaunchedEffect(state.snapCount) {
        if (state.snapCount > 0 && soundEnabled) {
            toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 120)
        }
    }
    LaunchedEffect(state.completed) {
        if (state.completed) onComplete(state.elapsedSeconds)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Column { Text("${level.id.toString().padStart(2, '0')} · ${level.name}"); Text(formatTime(state.elapsedSeconds), fontSize = 12.sp, color = MaterialTheme.colorScheme.outline) } },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Rounded.Close, "離開關卡") } },
                actions = {
                    if (!state.completed) IconButton(onClick = game::hint) { Icon(Icons.Rounded.Lightbulb, "提示") }
                    IconButton(onClick = game::reset) { Icon(Icons.Rounded.Refresh, "重置") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            if (!state.completed) GameControls(state.selected, game)
        }
    ) { padding ->
        val contentModifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(padding).padding(12.dp)
        if (state.completed && boardSize != IntSize.Zero) {
            val density = LocalDensity.current
            Column(
                contentModifier.navigationBarsPadding().padding(bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CompletedTangramBoard(
                    pieces = state.pieces,
                    virtualBoardSize = boardSize,
                    colorTheme = colorTheme,
                    modifier = Modifier.fillMaxWidth().height(with(density) { (boardSize.height * TARGET_AREA_BOTTOM).toDp() })
                )
                Spacer(Modifier.height(12.dp))
                CompletionPanel(
                    level = level,
                    elapsedSeconds = state.elapsedSeconds,
                    bestTime = bestTime,
                    hintsUsed = state.hintsUsed,
                    isCustom = isCustom,
                    onRetry = game::reset,
                    onShare = { shareCompletedLevel(context, displayLevel, state.pieces, colorTheme, state.elapsedSeconds) },
                    onNext = {
                        if (!isCustom && level.id < levels.size) navController.navigate("game/${level.id + 1}") { popUpTo("game/${level.id}") { inclusive = true } }
                        else navController.popBackStack("levels", inclusive = false)
                    },
                    onLevels = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )
            }
        } else {
            Box(contentModifier.onSizeChanged { if (boardSize == IntSize.Zero) boardSize = it }) {
                TangramBoard(
                    displayLevel,
                    state.pieces,
                    state.selected,
                    state.hint,
                    state.hintTarget,
                    game,
                    difficulty,
                    colorTheme,
                    Modifier.fillMaxSize()
                )
            }
        }
    }

}

@Composable
private fun CompletionPanel(
    level: Level,
    elapsedSeconds: Long,
    bestTime: Long?,
    hintsUsed: Int,
    isCustom: Boolean,
    onRetry: () -> Unit,
    onShare: () -> Unit,
    onNext: () -> Unit,
    onLevels: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .25f), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(level.name, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Text("完成時間 ${formatTime(elapsedSeconds)}", modifier = Modifier.padding(top = 2.dp))
        Text(
            when {
                bestTime == null -> "新的最佳紀錄"
                elapsedSeconds < bestTime -> "比前次紀錄快了 ${bestTime - elapsedSeconds} 秒"
                elapsedSeconds > bestTime -> "比最佳紀錄慢了 ${elapsedSeconds - bestTime} 秒"
                else -> "與最佳紀錄相同"
            },
            color = MaterialTheme.colorScheme.primary,
            fontSize = 13.sp
        )
        if (hintsUsed > 0) Text("使用提示 $hintsUsed 次", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onRetry) { Text("再次挑戰") }
            if (!isCustom) {
                Spacer(Modifier.width(8.dp))
                Button(onClick = onNext) { Text("前進下一關") }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onLevels) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "返回關卡選擇") }
            IconButton(onClick = onShare) { Icon(Icons.Rounded.Share, "分享圖案") }
        }
    }
}

@Composable
private fun CompletedTangramBoard(
    pieces: List<PlayingPiece>,
    virtualBoardSize: IntSize,
    colorTheme: PieceColorTheme,
    modifier: Modifier = Modifier
) {
    val surface = MaterialTheme.colorScheme.surface
    val outline = MaterialTheme.colorScheme.outline
    Canvas(
        modifier
            .background(surface, RoundedCornerShape(8.dp))
            .border(1.dp, outline.copy(alpha = .25f), RoundedCornerShape(8.dp))
    ) {
        val virtualHeight = virtualBoardSize.height.toFloat()
        val normalizedYScale = size.width / virtualHeight
        pieces.forEach { piece ->
            drawPieceAtVirtualHeight(
                piece = piece,
                virtualHeight = virtualHeight,
                normalizedYScale = normalizedYScale,
                fill = Color(piece.spec.kind.colorFor(colorTheme)).copy(alpha = .82f),
                stroke = Color.Black.copy(alpha = .35f),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}

@Composable
private fun GameControls(selected: PieceKind?, game: GameViewModel) {
    Row(
        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { game.rotate(-45) }, enabled = selected != null, modifier = Modifier.size(52.dp)) { Icon(Icons.AutoMirrored.Rounded.RotateLeft, "向左旋轉") }
        IconButton(onClick = { game.rotate(45) }, enabled = selected != null, modifier = Modifier.size(52.dp)) { Icon(Icons.AutoMirrored.Rounded.RotateRight, "向右旋轉") }
        Text(if (selected == null) "先選一塊拼片" else "已選取", color = MaterialTheme.colorScheme.outline, fontSize = 13.sp)
        IconButton(onClick = game::flip, enabled = selected == PieceKind.PARALLELOGRAM, modifier = Modifier.size(52.dp)) { Icon(Icons.Rounded.Flip, "換邊") }
    }
}

@Composable
private fun TangramBoard(
    level: Level,
    pieces: List<PlayingPiece>,
    selected: PieceKind?,
    hint: PieceKind?,
    hintTarget: PieceKind?,
    game: GameViewModel,
    difficulty: Difficulty,
    colorTheme: PieceColorTheme,
    modifier: Modifier = Modifier
) {
    val outline = MaterialTheme.colorScheme.outline
    val onSurface = MaterialTheme.colorScheme.onSurface
    val surface = MaterialTheme.colorScheme.surface
    val accent = MaterialTheme.colorScheme.primary
    val dark = surface.luminance() < .5f
    Canvas(
        modifier.background(surface, RoundedCornerShape(8.dp)).border(1.dp, outline.copy(alpha = .25f), RoundedCornerShape(8.dp))
            .pointerInput(level.id) {
                detectTapGestures { position ->
                    game.select(
                        Vec2(position.x / size.width, position.y / size.height),
                        size.width.toFloat() / size.height.toFloat()
                    )
                }
            }
            .pointerInput(level.id) {
                detectDragGestures(
                    onDragStart = {
                        game.select(
                            Vec2(it.x / size.width, it.y / size.height),
                            size.width.toFloat() / size.height.toFloat()
                        )
                    },
                    onDragEnd = game::finishGesture,
                    onDragCancel = game::finishGesture,
                    onDrag = { change, amount ->
                        change.consume()
                        game.drag(Vec2(amount.x / size.width, amount.y / size.height))
                    }
                )
            }
    ) {
        drawLine(
            onSurface.copy(alpha = if (dark) .34f else .18f),
            Offset(0f, size.height * TARGET_AREA_BOTTOM),
            Offset(size.width, size.height * TARGET_AREA_BOTTOM),
            1.dp.toPx()
        )
        val targetPieces = level.targets.map { (kind, pose) ->
            val spec = pieceSpecs.first { it.kind == kind }
            PlayingPiece(spec, pose)
        }
        if (difficulty == Difficulty.BEGINNER) {
            targetPieces.forEach { targetPiece ->
                val isHint = hintTarget == targetPiece.spec.kind
                drawPiece(
                    targetPiece,
                    if (isHint) accent.copy(alpha = .25f) else onSurface.copy(alpha = if (dark) .18f else .08f),
                    if (isHint) accent else onSurface.copy(alpha = if (dark) .78f else .48f),
                    2.dp.toPx()
                )
            }
        } else {
            val targetPath = targetUnionPath(targetPieces, size.width / size.height, size.width, size.height)
            drawPath(targetPath, onSurface.copy(alpha = if (dark) .14f else .07f))
            drawPath(targetPath, onSurface.copy(alpha = if (dark) .78f else .48f), style = Stroke(2.dp.toPx()))
            targetPieces.firstOrNull { it.spec.kind == hintTarget }?.let { targetPiece ->
                drawPiece(targetPiece, accent.copy(alpha = .25f), accent, 2.dp.toPx())
            }
        }
        pieces.forEach { piece ->
            val isSelected = selected == piece.spec.kind
            val isHint = hint == piece.spec.kind
            drawPiece(
                piece,
                Color(piece.spec.kind.colorFor(colorTheme)).copy(alpha = if (piece.snapped) .82f else 1f),
                when { isHint -> accent; isSelected -> if (dark) Color.White else Color(0xFF3F342C); else -> Color.Black.copy(alpha = .35f) },
                if (isSelected || isHint) 3.dp.toPx() else 1.dp.toPx()
            )
        }
    }
}

private fun DrawScope.drawPiece(piece: PlayingPiece, fill: Color, stroke: Color, strokeWidth: Float) {
    val vertices = transformedVertices(piece, size.width / size.height)
    val path = Path().apply {
        moveTo(vertices.first().x * size.width, vertices.first().y * size.height)
        vertices.drop(1).forEach { lineTo(it.x * size.width, it.y * size.height) }
        close()
    }
    drawPath(path, fill)
    drawPath(path, stroke, style = Stroke(strokeWidth))
}

private fun DrawScope.drawPieceAtVirtualHeight(
    piece: PlayingPiece,
    virtualHeight: Float,
    normalizedYScale: Float,
    fill: Color,
    stroke: Color,
    strokeWidth: Float
) {
    val vertices = transformedVertices(piece, normalizedYScale)
    val path = Path().apply {
        moveTo(vertices.first().x * size.width, vertices.first().y * virtualHeight)
        vertices.drop(1).forEach { lineTo(it.x * size.width, it.y * virtualHeight) }
        close()
    }
    drawPath(path, fill)
    drawPath(path, stroke, style = Stroke(strokeWidth))
}

@Composable
private fun SettingChoice(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    preview: (@Composable () -> Unit)? = null
) {
    Column(
        modifier
            .background(if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
            .border(if (selected) 2.dp else 1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = .4f), RoundedCornerShape(8.dp))
            .then(Modifier.selectable(selected = selected, role = androidx.compose.ui.semantics.Role.RadioButton, onClick = onClick))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, modifier = Modifier.padding(vertical = 6.dp), color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface, fontSize = 15.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
        preview?.invoke()
    }
}

private fun targetUnionPath(pieces: List<PlayingPiece>, normalizedYScale: Float, width: Float, height: Float): Path {
    val paths = pieces.map { piece ->
        val vertices = transformedVertices(piece, normalizedYScale)
        Path().apply {
            moveTo(vertices.first().x * width, vertices.first().y * height)
            vertices.drop(1).forEach { lineTo(it.x * width, it.y * height) }
            close()
        }
    }
    return paths.drop(1).fold(paths.first()) { result, path -> Path.combine(PathOperation.Union, result, path) }
}

@Composable
private fun EditorPreviewBoard(
    poses: Map<PieceKind, Pose>,
    sourceSize: IntSize,
    difficulty: Difficulty,
    modifier: Modifier = Modifier
) {
    val sourceScale = (if (sourceSize.height > 0) sourceSize.width.toFloat() / sourceSize.height else 1f).coerceAtLeast(.01f)
    val sourcePieces = poses.map { (kind, pose) -> PlayingPiece(pieceSpecs.first { it.kind == kind }, pose) }
    val sourceVertices = sourcePieces.map { piece ->
        piece to transformedVertices(piece, sourceScale).map { vertex -> Vec2(vertex.x, vertex.y / sourceScale) }
    }
    val vertices = sourceVertices.flatMap { it.second }
    val minX = vertices.minOfOrNull { it.x } ?: 0f
    val maxX = vertices.maxOfOrNull { it.x } ?: 1f
    val minY = vertices.minOfOrNull { it.y } ?: 0f
    val maxY = vertices.maxOfOrNull { it.y } ?: 1f
    val boundsWidth = (maxX - minX).coerceAtLeast(.0001f)
    val boundsHeight = (maxY - minY).coerceAtLeast(.0001f)
    val surface = MaterialTheme.colorScheme.surface
    val outline = MaterialTheme.colorScheme.outline
    val onSurface = MaterialTheme.colorScheme.onSurface
    val dark = surface.luminance() < .5f
    Canvas(modifier.background(surface, RoundedCornerShape(8.dp)).border(1.dp, outline.copy(alpha = .25f), RoundedCornerShape(8.dp))) {
        val margin = .05f
        val scale = minOf(size.width * (1f - margin * 2f) / boundsWidth, size.height * (1f - margin * 2f) / boundsHeight)
        val offsetX = (size.width - boundsWidth * scale) / 2f - minX * scale
        val offsetY = (size.height - boundsHeight * scale) / 2f - minY * scale
        val mappedPieces = sourceVertices.map { (piece, pieceVertices) ->
            piece to pieceVertices.map { vertex -> Offset(vertex.x * scale + offsetX, vertex.y * scale + offsetY) }
        }
        if (difficulty == Difficulty.BEGINNER) {
            mappedPieces.forEach { (_, pieceVertices) ->
                drawPreviewPolygon(pieceVertices, onSurface.copy(alpha = if (dark) .18f else .08f), onSurface.copy(alpha = if (dark) .78f else .48f), 2.dp.toPx())
            }
        } else {
            val paths = mappedPieces.map { (_, pieceVertices) ->
                Path().apply {
                    moveTo(pieceVertices.first().x, pieceVertices.first().y)
                    pieceVertices.drop(1).forEach { lineTo(it.x, it.y) }
                    close()
                }
            }
            val path = paths.drop(1).fold(paths.first()) { result, next -> Path.combine(PathOperation.Union, result, next) }
            drawPath(path, onSurface.copy(alpha = if (dark) .14f else .07f))
            drawPath(path, onSurface.copy(alpha = if (dark) .78f else .48f), style = Stroke(2.dp.toPx()))
        }
    }
}

private fun DrawScope.drawPreviewPolygon(vertices: List<Offset>, fill: Color, stroke: Color, strokeWidth: Float) {
    val path = Path().apply {
        moveTo(vertices.first().x, vertices.first().y)
        vertices.drop(1).forEach { lineTo(it.x, it.y) }
        close()
    }
    drawPath(path, fill)
    drawPath(path, stroke, style = Stroke(strokeWidth))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LevelEditorScreen(
    navController: NavController,
    repository: PreferencesRepository,
    existing: Level?,
    scope: kotlinx.coroutines.CoroutineScope,
    colorTheme: PieceColorTheme
) {
    val editorDark = MaterialTheme.colorScheme.surface.luminance() < .5f
    val guide = MaterialTheme.colorScheme.primary.copy(alpha = if (editorDark) .42f else .28f)
    val initial = existing?.targets ?: initialPiecePoses(1f)
    var poses by remember(existing?.id) { mutableStateOf(initial) }
    var selected by remember { mutableStateOf<PieceKind?>(null) }
    var moveWhole by remember { mutableStateOf(false) }
    var name by remember(existing?.id) { mutableStateOf(existing?.name ?: "") }
    var showNameDialog by remember { mutableStateOf(false) }
    var showPreview by remember { mutableStateOf(false) }
    var previewDifficulty by remember { mutableStateOf(Difficulty.BEGINNER) }
    var editorSize by remember { mutableStateOf(IntSize.Zero) }
    val snapPixels = with(LocalDensity.current) { 12.dp.toPx() }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existing == null) "建立關卡" else "編輯關卡") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Rounded.Close, "取消") } },
                actions = {
                    if (existing != null) {
                        IconButton(onClick = {
                            scope.launch {
                                repository.deleteCustomLevel(existing.id)
                                navController.popBackStack("levels", inclusive = false)
                            }
                        }) { Icon(Icons.Rounded.Delete, "刪除關卡") }
                    }
                    IconButton(onClick = { poses = centerEditorPoses(poses, editorSize.takeIf { it.height > 0 }?.let { it.width.toFloat() / it.height } ?: 1f) }) {
                        Icon(Icons.Rounded.CenterFocusStrong, "自動置中")
                    }
                    IconButton(onClick = { showPreview = true }) { Icon(Icons.Rounded.Visibility, "關卡預覽") }
                    TextButton(onClick = { showNameDialog = true }) { Text("儲存") }
                }
            )
        },
        bottomBar = {
            Row(Modifier.fillMaxWidth().navigationBarsPadding().background(MaterialTheme.colorScheme.surface).padding(8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                IconButton(enabled = selected != null && !moveWhole, onClick = { selected?.let { kind -> poses = poses + (kind to poses.getValue(kind).copy(rotation = (poses.getValue(kind).rotation + 315) % 360)) } }) { Icon(Icons.AutoMirrored.Rounded.RotateLeft, "向左旋轉") }
                IconButton(enabled = selected != null && !moveWhole, onClick = { selected?.let { kind -> poses = poses + (kind to poses.getValue(kind).copy(rotation = (poses.getValue(kind).rotation + 45) % 360)) } }) { Icon(Icons.AutoMirrored.Rounded.RotateRight, "向右旋轉") }
                Text(if (moveWhole) "整體移動中" else if (selected == null) "兩指移動整個圖案" else "已選取", modifier = Modifier.align(Alignment.CenterVertically), color = MaterialTheme.colorScheme.outline, fontSize = 12.sp)
                IconButton(enabled = selected == PieceKind.PARALLELOGRAM && !moveWhole, onClick = { selected?.let { kind -> poses = poses + (kind to poses.getValue(kind).copy(flipped = !poses.getValue(kind).flipped)) } }) { Icon(Icons.Rounded.Flip, "換邊") }
            }
        }
    ) { padding ->
        Canvas(
            Modifier.fillMaxSize().padding(padding).padding(12.dp).background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                .onSizeChanged {
                    if (it.width > 0 && it.height > 0) {
                        val newScale = it.width.toFloat() / it.height
                        if (editorSize == IntSize.Zero && existing == null) {
                            poses = initialPiecePoses(newScale)
                        } else {
                            val oldScale = if (editorSize.height > 0) editorSize.width.toFloat() / editorSize.height else existing?.canvasYScale
                            poses = Level(0, "", poses, oldScale).forCanvas(newScale).targets
                        }
                    }
                    editorSize = it
                }
                .pointerInput(snapPixels) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        val start = poses
                        val scale = size.width.toFloat() / size.height
                        selected = poses.entries.toList().asReversed().firstOrNull { (kind, pose) ->
                            containsPoint(transformedVertices(PlayingPiece(pieceSpecs.first { it.kind == kind }, pose), scale), Vec2(down.position.x / size.width, down.position.y / size.height))
                        }?.key
                        var whole = false
                        var dragged = false
                        try {
                            do {
                                val event = awaitPointerEvent()
                                val active = event.changes.filter { it.pressed }
                                if (active.size >= 2 && !whole) {
                                    poses = start
                                    whole = true
                                    moveWhole = true
                                } else if (active.size >= 2 || (!whole && active.size == 1)) {
                                    val stable = active.filter { it.previousPressed }
                                    if (stable.isNotEmpty()) {
                                        val dx = stable.sumOf { (it.position.x - it.previousPosition.x).toDouble() }.toFloat() / stable.size / size.width
                                        val dy = stable.sumOf { (it.position.y - it.previousPosition.y).toDouble() }.toFloat() / stable.size / size.height
                                        if (whole) {
                                            val vertices = poses.flatMap { (kind, pose) -> transformedVertices(PlayingPiece(pieceSpecs.first { it.kind == kind }, pose), scale) }
                                            fun bounded(delta: Float, low: Float, high: Float) = if (low <= high) delta.coerceIn(low, high) else 0f
                                            val shift = Vec2(bounded(dx, -vertices.minOf { it.x }, 1f - vertices.maxOf { it.x }), bounded(dy, -vertices.minOf { it.y }, 1f - vertices.maxOf { it.y }))
                                            poses = poses.mapValues { (_, pose) -> pose.copy(center = pose.center + shift) }
                                        } else selected?.let { kind ->
                                            if (dx != 0f || dy != 0f) dragged = true
                                            val pose = poses.getValue(kind)
                                            poses = poses + (kind to pose.copy(center = Vec2((pose.center.x + dx).coerceIn(.05f, .95f), (pose.center.y + dy).coerceIn(.06f, .94f))))
                                        }
                                    }
                                }
                                event.changes.forEach { it.consume() }
                            } while (event.changes.any { it.pressed })
                            if (!whole && dragged) selected?.let { kind ->
                                poses = poses + (kind to snapEditorPose(kind, poses.getValue(kind), poses.filterKeys { it != kind }, scale, snapPixels / size.width))
                            }
                        } finally { moveWhole = false }
                    }
                }
        ) {
            val guideStroke = Stroke(1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 6.dp.toPx())))
            drawRect(
                color = guide.copy(alpha = if (editorDark) .08f else .05f),
                size = androidx.compose.ui.geometry.Size(size.width, size.height * TARGET_AREA_BOTTOM)
            )
            drawRect(
                color = guide,
                size = androidx.compose.ui.geometry.Size(size.width, size.height * TARGET_AREA_BOTTOM),
                style = guideStroke
            )
            drawLine(
                color = guide,
                start = Offset(size.width / 2f, 0f),
                end = Offset(size.width / 2f, size.height * TARGET_AREA_BOTTOM),
                strokeWidth = 1.dp.toPx(),
                pathEffect = guideStroke.pathEffect
            )
            drawLine(
                color = guide,
                start = Offset(0f, size.height * TARGET_AREA_BOTTOM / 2f),
                end = Offset(size.width, size.height * TARGET_AREA_BOTTOM / 2f),
                strokeWidth = 1.dp.toPx(),
                pathEffect = guideStroke.pathEffect
            )
            poses.forEach { (kind, pose) ->
                drawPiece(PlayingPiece(pieceSpecs.first { it.kind == kind }, pose), Color(kind.colorFor(colorTheme)), if (selected == kind) { if (editorDark) Color.White else Color(0xFF3F342C) } else Color.Black.copy(alpha = .35f), if (selected == kind) 3.dp.toPx() else 1.dp.toPx())
            }
        }
    }
    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text("儲存關卡") },
            text = { TextField(value = name, onValueChange = { name = it }, label = { Text("關卡名稱") }, singleLine = true) },
            confirmButton = {
                Button(enabled = name.isNotBlank() && editorSize.width > 0 && editorSize.height > 0, onClick = {
                    scope.launch {
                        repository.saveCustomLevel(Level(existing?.id ?: 0, name.trim(), poses, editorSize.width.toFloat() / editorSize.height))
                        navController.popBackStack()
                    }
                }) { Text("儲存") }
            },
            dismissButton = { TextButton(onClick = { showNameDialog = false }) { Text("取消") } }
        )
    }
    if (showPreview) {
        AlertDialog(
            onDismissRequest = { showPreview = false },
            title = { Text("關卡預覽") },
            text = {
                Column {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Difficulty.entries.forEach { mode ->
                            SettingChoice(
                                if (mode == Difficulty.BEGINNER) "初級" else "高級",
                                previewDifficulty == mode,
                                { previewDifficulty = mode },
                                Modifier.weight(1f)
                            )
                        }
                    }
                    EditorPreviewBoard(
                        poses,
                        editorSize,
                        previewDifficulty,
                        Modifier.fillMaxWidth().height(260.dp).padding(top = 12.dp).clipToBounds()
                    )
                }
            },
            confirmButton = { TextButton(onClick = { showPreview = false }) { Text("關閉") } }
        )
    }
}

@Composable
private fun PaperBackground(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Canvas(Modifier.fillMaxSize()) {
            val ink = Color(0xFF8B7661).copy(alpha = .055f)
            for (i in 0..12) drawLine(ink, Offset(0f, i * size.height / 12), Offset(size.width, i * size.height / 12 + 18f), 1f)
        }
        content()
    }
}

@Composable
private fun LauncherTangram(modifier: Modifier = Modifier) {
    Box(modifier.background(Color(0xFFFFE9B8), RoundedCornerShape(28.dp)), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = "彩色七巧板",
            modifier = Modifier.fillMaxSize()
        )
    }
}

private fun formatTime(seconds: Long): String = "%02d:%02d".format(seconds / 60, seconds % 60)

private fun shareCompletedLevel(
    context: Context,
    level: Level,
    pieces: List<PlayingPiece>,
    colorTheme: PieceColorTheme,
    elapsedSeconds: Long
) {
    val bitmap = Bitmap.createBitmap(1080, 1080, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)
    canvas.drawColor(android.graphics.Color.rgb(242, 232, 213))
    val sourceScale = (level.canvasYScale ?: 1f).coerceAtLeast(.01f)
    val pieceVertices = pieces.map { piece ->
        piece.spec.kind to transformedVertices(piece, sourceScale).map { vertex ->
            Vec2(vertex.x, vertex.y / sourceScale)
        }
    }
    val vertices = pieceVertices.flatMap { it.second }
    val minX = vertices.minOf { it.x }
    val maxX = vertices.maxOf { it.x }
    val minY = vertices.minOf { it.y }
    val maxY = vertices.maxOf { it.y }
    val boundsWidth = (maxX - minX).coerceAtLeast(.0001f)
    val boundsHeight = (maxY - minY).coerceAtLeast(.0001f)
    val margin = 72f
    val scale = minOf((bitmap.width - margin * 2f) / boundsWidth, (bitmap.height - margin * 2f) / boundsHeight)
    val offsetX = (bitmap.width - boundsWidth * scale) / 2f - minX * scale
    val offsetY = (bitmap.height - boundsHeight * scale) / 2f - minY * scale
    val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = android.graphics.Color.argb(100, 45, 38, 32)
    }
    pieceVertices.forEach { (kind, polygon) ->
        val path = AndroidPath().apply {
            moveTo(polygon.first().x * scale + offsetX, polygon.first().y * scale + offsetY)
            polygon.drop(1).forEach { lineTo(it.x * scale + offsetX, it.y * scale + offsetY) }
            close()
        }
        fill.color = kind.colorFor(colorTheme).toInt()
        canvas.drawPath(path, fill)
        canvas.drawPath(path, stroke)
    }
    val directory = File(context.cacheDir, "shared").apply { mkdirs() }
    val image = File(directory, "${level.name}-completed.png")
    FileOutputStream(image).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", image)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, "${level.name} · 完成時間 ${formatTime(elapsedSeconds)}")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "分享完成圖案"))
}
