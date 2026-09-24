package com.example.minitangram

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Flip
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
import com.example.minitangram.game.Level
import com.example.minitangram.game.PieceKind
import com.example.minitangram.game.PlayingPiece
import com.example.minitangram.game.Pose
import com.example.minitangram.game.Vec2
import com.example.minitangram.game.levels
import com.example.minitangram.game.pieceSpecs
import com.example.minitangram.game.transformedVertices
import com.example.minitangram.ui.theme.DisplayMode
import com.example.minitangram.ui.theme.MiniTangramTheme
import kotlinx.coroutines.launch

@Composable
fun MiniTangramApp(onExit: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { PreferencesRepository(context.applicationContext) }
    val progress by repository.progress.collectAsStateWithLifecycle(UserProgress())
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

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
                LevelSelectScreen(navController, progress)
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
                    onSoundEnabledChange = { enabled -> scope.launch { repository.setSoundEnabled(enabled) } }
                )
            }
            composable(
                route = "game/{levelId}",
                arguments = listOf(navArgument("levelId") { type = NavType.IntType })
            ) { entry ->
                val levelId = entry.arguments?.getInt("levelId") ?: 1
                val level = levels.first { it.id == levelId }
                GameScreen(
                    navController = navController,
                    level = level,
                    bestTime = progress.bestTimes[levelId],
                    soundEnabled = progress.soundEnabled,
                    onComplete = { seconds -> repository.completeLevel(levelId, seconds) }
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
            Spacer(Modifier.weight(.32f))
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
            Spacer(Modifier.weight(.5f))
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
                Text("挑戰十二種動物剪影", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .78f), fontSize = 12.sp)
            }
            Text("壹", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .45f), fontSize = 24.sp, fontWeight = FontWeight.Bold)
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
private fun PageScaffold(title: String, navController: NavController, content: @Composable (PaddingValues) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.SemiBold) },
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
private fun LevelSelectScreen(navController: NavController, progress: UserProgress) {
    PageScaffold("選擇關卡", navController) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(145.dp),
            contentPadding = PaddingValues(
                start = 18.dp,
                end = 18.dp,
                top = padding.calculateTopPadding() + 12.dp,
                bottom = padding.calculateBottomPadding() + 24.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
        ) {
            items(levels) { level ->
                val unlocked = level.id <= progress.unlockedLevel
                val best = progress.bestTimes[level.id]
                Card(
                    modifier = Modifier.height(132.dp).clickable(enabled = unlocked) { navController.navigate("game/${level.id}") },
                    shape = RoundedCornerShape(6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (unlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(level.id.toString().padStart(2, '0'), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Icon(
                                if (unlocked) Icons.Rounded.Check else Icons.Rounded.Lock,
                                if (unlocked) "已解鎖" else "未解鎖",
                                tint = if (best != null) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
                            )
                        }
                        Text(level.name, fontSize = 23.sp, fontWeight = FontWeight.Medium)
                        Text(best?.let { "最佳 ${formatTime(it)}" } ?: if (unlocked) "尚未完成" else "完成前一關解鎖", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
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
    onSoundEnabledChange: (Boolean) -> Unit
) {
    PageScaffold("設定", navController) { padding ->
        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(padding).padding(24.dp)) {
            Text("顯示模式", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Text("選擇適合環境的畫面明暗", color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(top = 4.dp, bottom = 16.dp))
            DisplayMode.entries.forEach { mode ->
                val label = when (mode) { DisplayMode.SYSTEM -> "系統"; DisplayMode.LIGHT -> "淺色"; DisplayMode.DARK -> "深色" }
                Row(
                    Modifier.fillMaxWidth().clickable { onModeChange(mode) }.padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = currentMode == mode, onClick = { onModeChange(mode) })
                    Spacer(Modifier.width(10.dp))
                    Text(label, fontSize = 17.sp)
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 20.dp))
            Row(
                Modifier.fillMaxWidth().clickable { onSoundEnabledChange(!soundEnabled) }.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Text("音效", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    Text("拼片正確吸附時播放提示音", color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(top = 4.dp))
                }
                Switch(checked = soundEnabled, onCheckedChange = onSoundEnabledChange)
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
    onComplete: suspend (Long) -> Unit
) {
    val factory = remember(level.id) {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = GameViewModel(level) as T
        }
    }
    val game: GameViewModel = viewModel(key = "game-${level.id}", factory = factory)
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
                    IconButton(onClick = game::hint) { Icon(Icons.Rounded.Lightbulb, "提示") }
                    IconButton(onClick = game::reset) { Icon(Icons.Rounded.Refresh, "重置") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = { GameControls(state.selected, game) }
    ) { padding ->
        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(padding)) {
            Text("依照淡墨剪影拼合七片", color = MaterialTheme.colorScheme.outline, fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp))
            TangramBoard(
                level,
                state.pieces,
                state.selected,
                state.hint,
                state.hintTarget,
                game,
                Modifier.fillMaxWidth().weight(1f).padding(12.dp)
            )
        }
    }

    if (state.completed) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("巧成 · ${level.name}") },
            text = { Column { Text("完成時間 ${formatTime(state.elapsedSeconds)}"); Text(if (bestTime == null || state.elapsedSeconds < bestTime) "新的最佳紀錄" else "最佳紀錄 ${formatTime(bestTime)}", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp)); if (state.hintsUsed > 0) Text("使用提示 ${state.hintsUsed} 次", fontSize = 13.sp, color = MaterialTheme.colorScheme.outline) } },
            confirmButton = {
                Button(onClick = {
                    if (level.id < levels.size) navController.navigate("game/${level.id + 1}") { popUpTo("game/${level.id}") { inclusive = true } }
                    else navController.navigate("levels") { popUpTo("levels") { inclusive = false } }
                }) { Text(if (level.id < levels.size) "下一關" else "返回關卡") }
            },
            dismissButton = { TextButton(onClick = { navController.popBackStack() }) { Text("關卡選擇") } }
        )
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
        Text(if (selected == null) "先選一塊拼片" else "已選取", color = MaterialTheme.colorScheme.outline, fontSize = 13.sp)
        IconButton(onClick = game::flip, enabled = selected == PieceKind.PARALLELOGRAM, modifier = Modifier.size(52.dp)) { Icon(Icons.Rounded.Flip, "翻面") }
        IconButton(onClick = { game.rotate(45) }, enabled = selected != null, modifier = Modifier.size(52.dp)) { Icon(Icons.AutoMirrored.Rounded.RotateRight, "向右旋轉") }
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
            Offset(0f, size.height * .68f),
            Offset(size.width, size.height * .68f),
            1.dp.toPx()
        )
        level.targets.forEach { (kind, pose) ->
            val spec = pieceSpecs.first { it.kind == kind }
            val targetPiece = PlayingPiece(spec, pose)
            val isHint = hintTarget == kind
            drawPiece(
                targetPiece,
                if (isHint) accent.copy(alpha = .25f) else onSurface.copy(alpha = if (dark) .18f else .08f),
                if (isHint) accent else onSurface.copy(alpha = if (dark) .78f else .48f),
                2.dp.toPx()
            )
        }
        pieces.forEach { piece ->
            val isSelected = selected == piece.spec.kind
            val isHint = hint == piece.spec.kind
            drawPiece(
                piece,
                Color(piece.spec.color).copy(alpha = if (piece.snapped) .82f else 1f),
                when { isHint -> accent; isSelected -> Color.White; else -> Color.Black.copy(alpha = .35f) },
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
