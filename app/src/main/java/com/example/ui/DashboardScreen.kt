package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserProfile
import com.example.data.WaterLog
import com.example.data.CustomContainer
import com.example.viewmodel.WaterViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: WaterViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val profile by viewModel.userProfile.collectAsState()
    val todayLogs by viewModel.todayLogs.collectAsState()
    val totalIntake by viewModel.totalIntakeToday.collectAsState()
    val dailyGoal by viewModel.dailyGoal.collectAsState()
    val progress by viewModel.intakeProgress.collectAsState()
    val customContainers by viewModel.customContainers.collectAsState()

    var showCupSheet by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    // Alarms state
    var remindersEnabled by remember { mutableStateOf(true) }
    var intervalMinutes by remember { mutableStateOf(60f) }

    var selectedTab by remember { mutableStateOf(0) }

    // Keep state in sync with loaded profile
    LaunchedEffect(profile) {
        profile?.let {
            remindersEnabled = it.remindersEnabled
            intervalMinutes = it.reminderIntervalMinutes.toFloat()
        }
    }

    val isDark = isSystemInDarkTheme()
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = if (isDark) Color(0xFF1E282A) else Color(0xFFF0F4F4),
                contentColor = if (isDark) Color.White else Color(0xFF001F24)
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = if (isDark) Color(0xFFCCE8E8) else Color(0xFF002020),
                        selectedTextColor = if (isDark) Color(0xFFCCE8E8) else Color(0xFF002020),
                        indicatorColor = if (isDark) Color(0xFF004F4F) else Color(0xFFCCE8E8)
                    ),
                    modifier = Modifier.testTag("tab_home")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Star, contentDescription = "Progress") },
                    label = { Text("Progress") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = if (isDark) Color(0xFFCCE8E8) else Color(0xFF002020),
                        selectedTextColor = if (isDark) Color(0xFFCCE8E8) else Color(0xFF002020),
                        indicatorColor = if (isDark) Color(0xFF004F4F) else Color(0xFFCCE8E8)
                    ),
                    modifier = Modifier.testTag("tab_progress")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Build, contentDescription = "Containers") },
                    label = { Text("Containers") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = if (isDark) Color(0xFFCCE8E8) else Color(0xFF002020),
                        selectedTextColor = if (isDark) Color(0xFFCCE8E8) else Color(0xFF002020),
                        indicatorColor = if (isDark) Color(0xFF004F4F) else Color(0xFFCCE8E8)
                    ),
                    modifier = Modifier.testTag("tab_containers")
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { showCupSheet = true },
                    containerColor = if (isDark) Color(0xFF006A6A) else Color(0xFF006A6A),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .padding(bottom = 16.dp, end = 8.dp)
                        .size(64.dp)
                        .testTag("fab_log_water")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Log Water Intake",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)
                        )
                    )
                )
        ) {
            when (selectedTab) {
                0 -> HomeScreenContent(
                    viewModel = viewModel,
                    todayLogs = todayLogs,
                    totalIntake = totalIntake,
                    dailyGoal = dailyGoal,
                    progress = progress,
                    customContainers = customContainers,
                    remindersEnabled = remindersEnabled,
                    intervalMinutes = intervalMinutes,
                    onUpdateReminders = { enabled, interval ->
                        remindersEnabled = enabled
                        intervalMinutes = interval
                    },
                    onShowResetDialog = { showResetDialog = true },
                    isDark = isDark
                )
                1 -> ProgressScreenContent(
                    viewModel = viewModel,
                    isDark = isDark
                )
                2 -> ContainersScreenContent(
                    viewModel = viewModel,
                    customContainers = customContainers,
                    isDark = isDark
                )
            }

            // Cup Sizes Bottom Sheet
            if (showCupSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showCupSheet = false },
                    sheetState = rememberModalBottomSheetState(),
                    containerColor = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    CupSelectionContent(
                        customContainers = customContainers,
                        onCupSelected = { amount ->
                            viewModel.logWater(amount)
                            showCupSheet = false
                        }
                    )
                }
            }

            // Confirm Reset Dialog
            if (showResetDialog) {
                AlertDialog(
                    onDismissRequest = { showResetDialog = false },
                    title = { Text("Reset Profile Data?") },
                    text = { Text("This will delete your personalized settings, calculated intake goal, and all log history. Are you sure you want to proceed?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.resetData()
                                showResetDialog = false
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Reset Everything")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showResetDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun HomeScreenContent(
    viewModel: WaterViewModel,
    todayLogs: List<WaterLog>,
    totalIntake: Int,
    dailyGoal: Int,
    progress: Float,
    customContainers: List<CustomContainer>,
    remindersEnabled: Boolean,
    intervalMinutes: Float,
    onUpdateReminders: (Boolean, Float) -> Unit,
    onShowResetDialog: () -> Unit,
    isDark: Boolean
) {
    var remindersEnabledState by remember(remindersEnabled) { mutableStateOf(remindersEnabled) }
    var intervalMinutesState by remember(intervalMinutes) { mutableStateOf(intervalMinutes) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 100.dp, top = 16.dp)
    ) {
        // Top Bento Header Row
        item {
            val sdf = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hydrate",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-1).sp,
                            fontSize = 28.sp
                        ),
                        color = if (isDark) Color.White else Color(0xFF001F24)
                    )
                    Text(
                        text = sdf.format(Date()),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF3F484B)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onShowResetDialog,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("btn_reset_all")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Profile",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF002020) else Color(0xFFCCE8E8)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "JD",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isDark) Color(0xFFCCE8E8) else Color(0xFF002020)
                        )
                    }
                }
            }
        }

        // 1. Bento Blue Hero Progress Card
        item {
            val heroBg = if (isDark) Color(0xFF1E2F46) else Color(0xFFD1E4FF)
            val heroText = if (isDark) Color(0xFFDFE2E9) else Color(0xFF001D36)
            val heroSubText = if (isDark) Color(0xFF90CAF9) else Color(0xFF003355)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = heroBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    WaterProgressRing(
                        progress = progress,
                        totalIntake = totalIntake,
                        modifier = Modifier,
                        primaryColor = if (isDark) Color(0xFF90CAF9) else Color(0xFF00639B),
                        trackColor = if (isDark) Color(0xFF3E474F) else Color(0xFFE0E2EC),
                        textColor = heroText
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Daily Goal: $dailyGoal ml",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = heroSubText
                    )
                    Text(
                        text = if (totalIntake >= dailyGoal) "Target met! 🎉 Keep it up!" else "${(dailyGoal - totalIntake).coerceAtLeast(0)} ml left to hydrate",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = heroSubText.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        // Quick log option row - Alongside standard ones
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Quick Log Water",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        QuickLogChip(name = "Standard", volume = 250, emoji = "🥛", onClick = { viewModel.logWater(250) })
                    }
                    item {
                        QuickLogChip(name = "Bottle", volume = 500, emoji = "🥤", onClick = { viewModel.logWater(500) })
                    }
                    items(customContainers, key = { it.id }) { container ->
                        QuickLogChip(
                            name = container.name,
                            volume = container.volumeMl,
                            emoji = container.emoji,
                            onClick = { viewModel.logWater(container.volumeMl) }
                        )
                    }
                }
            }
        }

        // 2. Bento Info Grid Row (Two columns)
        item {
            val leftBorderColor = if (isDark) Color(0xFF3F484B) else Color(0xFFE0E2EC)
            val rightBgColor = if (isDark) Color(0xFF1B3232) else Color(0xFFE0F3F3)
            val rightOnColor = if (isDark) Color(0xFFCCE8E8) else Color(0xFF002020)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(115.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, leftBorderColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF2E3838) else Color(0xFFE0F2F1)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("💧", fontSize = 12.sp)
                        }

                        Column {
                            Text(
                                text = "${todayLogs.size}",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (isDark) Color.White else Color(0xFF191C1E)
                            )
                            Text(
                                text = "LOGS TODAY",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF3F484B)
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(115.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = rightBgColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF132323) else Color(0xFFCCE8E8)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🔔", fontSize = 12.sp)
                        }

                        Column {
                            val formattedTimeVal = if (remindersEnabledState) "${intervalMinutesState.toInt()}m" else "--"
                            Text(
                                text = formattedTimeVal,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = rightOnColor
                            )
                            Text(
                                text = "ALERT TIMER",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = rightOnColor.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        // 3. Bento Reminder Control Card
        item {
            val borderStrokeColor = if (isDark) Color(0xFF3F484B) else Color(0xFFE0E2EC)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, borderStrokeColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Reminder Interval",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = if (isDark) Color.White else Color(0xFF191C1E)
                            )
                            Text(
                                text = if (remindersEnabledState) "Smart Alerts: Active" else "Smart Alerts: Paused",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (remindersEnabledState) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (remindersEnabledState) {
                                Text(
                                    text = formatMinutes(intervalMinutesState.toInt()),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isDark) Color(0xFF90CAF9) else Color(0xFF00639B)
                                )
                            }
                            Switch(
                                checked = remindersEnabledState,
                                onCheckedChange = { enabled ->
                                    remindersEnabledState = enabled
                                    onUpdateReminders(enabled, intervalMinutesState)
                                    viewModel.updateReminders(enabled, intervalMinutesState.toInt())
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                    checkedTrackColor = if (isDark) Color(0xFF90CAF9) else Color(0xFF00639B)
                                ),
                                modifier = Modifier.testTag("switch_reminders")
                            )
                        }
                    }

                    if (remindersEnabledState) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Slider(
                            value = intervalMinutesState,
                            onValueChange = { intervalMinutesState = it },
                            onValueChangeFinished = {
                                onUpdateReminders(remindersEnabledState, intervalMinutesState)
                                viewModel.updateReminders(remindersEnabledState, intervalMinutesState.toInt())
                            },
                            valueRange = 25f..105f,
                            steps = 15,
                            colors = SliderDefaults.colors(
                                thumbColor = if (isDark) Color(0xFF90CAF9) else Color(0xFF00639B),
                                activeTrackColor = if (isDark) Color(0xFF90CAF9) else Color(0xFF00639B),
                                inactiveTrackColor = if (isDark) Color(0xFF3F484B) else Color(0xFFDEE3EB)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("slider_interval")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "25m",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF74777F)
                            )
                            Text(
                                text = "1h 45m",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF74777F)
                            )
                        }
                    }
                }
            }
        }

        // Today's Log Header
        item {
            Text(
                text = "Today's Log",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        if (todayLogs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.06f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🥤",
                            fontSize = 32.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No intake recorded yet.",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                        )
                        Text(
                            text = "Tap a Quick Log block or the floating action button below to track water.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp).padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            items(todayLogs, key = { it.id }) { log ->
                HydrationLogItem(
                    log = log,
                    onDelete = { viewModel.deleteLog(log.id) }
                )
            }
        }
    }
}

@Composable
fun ProgressScreenContent(
    viewModel: WaterViewModel,
    isDark: Boolean
) {
    val currentStreak by viewModel.currentStreak.collectAsState()
    val maxStreak by viewModel.maxStreak.collectAsState()
    val achievements by viewModel.achievements.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 100.dp, top = 16.dp)
    ) {
        // Top Header Row
        item {
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                Text(
                    text = "Achievements",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1).sp,
                        fontSize = 28.sp
                    ),
                    color = if (isDark) Color.White else Color(0xFF001F24)
                )
                Text(
                    text = "Track your daily streaks and unlocked badges.",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF3F484B)
                )
            }
        }

        // Streak Bento Grid Card
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Current Streak Card
                val warmBg = if (isDark) Color(0xFF422B13) else Color(0xFFFFECE0)
                val warmText = if (isDark) Color(0xFFFFDCC0) else Color(0xFF752B00)
                Card(
                    modifier = Modifier.weight(1f).height(125.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = warmBg)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) Color(0xFF5A3B18) else Color(0xFFFFDAB9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🔥", fontSize = 14.sp)
                            }
                            Text(
                                text = "Active",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = warmText.copy(alpha = 0.7f)
                            )
                        }
                        Column {
                            Text(
                                text = "$currentStreak days",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = warmText
                            )
                            Text(
                                text = "CURRENT STREAK",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = warmText.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // Best Streak Card
                val mBg = if (isDark) Color(0xFF2B3A1C) else Color(0xFFE8F5E9)
                val mText = if (isDark) Color(0xFFC8E6C9) else Color(0xFF1B5E20)
                Card(
                    modifier = Modifier.weight(1f).height(125.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = mBg)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) Color(0xFF1B4E1B) else Color(0xFFC8E6C9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🏆", fontSize = 14.sp)
                            }
                            Text(
                                text = "Best",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = mText.copy(alpha = 0.7f)
                            )
                        }
                        Column {
                            Text(
                                text = "$maxStreak days",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = mText
                            )
                            Text(
                                text = "ALL-TIME PEAK",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = mText.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }

        // Achievements Header Row
        item {
            Text(
                text = "Badges & Criteria",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
            )
        }

        // Achievements List
        items(achievements, key = { it.id }) { achievement ->
            AchievementCard(achievement = achievement, isDark = isDark)
        }
    }
}

@Composable
fun AchievementCard(
    achievement: com.example.viewmodel.Achievement,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val borderColor = if (achievement.isUnlocked) {
        if (isDark) Color(0x334CAF50) else Color(0x334CAF50)
    } else {
        if (isDark) Color(0xFF323B3E) else Color(0xFFE0E2EC)
    }
    
    val badgeBg = if (achievement.isUnlocked) {
        if (isDark) Color(0xFF203B23) else Color(0xFFE8F5E9)
    } else {
        if (isDark) Color(0xFF252D30) else Color(0xFFF3F4F6)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Badge
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(badgeBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = achievement.emoji,
                    fontSize = 24.sp,
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info column
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = achievement.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isDark) Color.White else Color(0xFF191C1E)
                    )
                    
                    if (achievement.isUnlocked) {
                        Text(
                            text = "Unlocked!",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
                        )
                    } else {
                        Text(
                            text = achievement.progressText,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                }
                
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                )

                // Progress Bar
                LinearProgressIndicator(
                    progress = { achievement.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = if (achievement.isUnlocked) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                    trackColor = if (isDark) Color(0xFF2E3537) else Color(0xFFE2EAF1)
                )
            }
        }
    }
}

@Composable
fun ContainersScreenContent(
    viewModel: WaterViewModel,
    customContainers: List<CustomContainer>,
    isDark: Boolean
) {
    var containerName by remember { mutableStateOf("") }
    var containerVolumeStr by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("🥤") }

    val emojis = listOf("🥛", "🥤", "☕", "🧪", "🍶", "🍼", "🧊")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 100.dp, top = 16.dp)
    ) {
        // Top Header Row
        item {
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                Text(
                    text = "My Containers",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1).sp,
                        fontSize = 28.sp
                    ),
                    color = if (isDark) Color.White else Color(0xFF001F24)
                )
                Text(
                    text = "Define and save your custom drinking vessels.",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF3F484B)
                )
            }
        }

        // Form Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isDark) Color(0xFF3F484B) else Color(0xFFE0E2EC)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Create Custom Container",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isDark) Color.White else Color(0xFF191C1E)
                    )

                    OutlinedTextField(
                        value = containerName,
                        onValueChange = { containerName = it },
                        label = { Text("Container Name") },
                        placeholder = { Text("e.g. My Water Bottle") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_container_name"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = containerVolumeStr,
                        onValueChange = {
                            if (it.isEmpty() || (it.length <= 4 && it.all { char -> char.isDigit() })) {
                                containerVolumeStr = it
                            }
                        },
                        label = { Text("Volume (ml)") },
                        placeholder = { Text("e.g. 750") },
                        suffix = { Text("ml") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_container_volume"),
                        singleLine = true
                    )

                    // Emoji choosing row
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Select Emoji",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            emojis.forEach { emoji ->
                                val isSelected = selectedEmoji == emoji
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) {
                                                if (isDark) Color(0xFF004F4F) else Color(0xFFCCE8E8)
                                            } else Color.Transparent
                                        )
                                        .clickable { selectedEmoji = emoji }
                                        .testTag("emoji_select_$emoji"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = emoji, fontSize = 20.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            val name = containerName.trim()
                            val amt = containerVolumeStr.toIntOrNull() ?: 0
                            if (name.isNotEmpty() && amt > 0) {
                                viewModel.addCustomContainer(name, amt, selectedEmoji)
                                containerName = ""
                                containerVolumeStr = ""
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_save_container"),
                        enabled = containerName.trim().isNotEmpty() && (containerVolumeStr.toIntOrNull() ?: 0) > 0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDark) Color(0xFF006A6A) else Color(0xFF006A6A)
                        )
                    ) {
                        Text("Save Container", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section Title: My Containers
        item {
            Text(
                text = "Saved Custom Sizes",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (customContainers.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🧪", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No custom containers saved yet.",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                        )
                        Text(
                            text = "Add custom tracker cup sizes using the creator above.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp).padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            items(customContainers, key = { it.id }) { container ->
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("container_item_${container.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isDark) Color(0xFF2E3537) else Color(0xFFE2EAF1)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = container.emoji, fontSize = 20.sp)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = container.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isDark) Color.White else Color(0xFF191C1E)
                                )
                                Text(
                                    text = "${container.volumeMl} ml",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.deleteCustomContainer(container.id) },
                            modifier = Modifier.size(36.dp).testTag("delete_container_${container.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete custom container",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickLogChip(
    name: String,
    volume: Int,
    emoji: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .widthIn(min = 110.dp)
            .height(58.dp)
            .testTag("quick_log_${volume}_ml"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = emoji, fontSize = 18.sp)
            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${volume}ml",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun CupSelectionContent(
    customContainers: List<CustomContainer>,
    onCupSelected: (Int) -> Unit
) {
    val presets = listOf(
        PresetCup(150, "Espresso Cup", "☕"),
        PresetCup(250, "Standard Glass", "🥛"),
        PresetCup(330, "Tall Bottle / Can", "🥤"),
        PresetCup(500, "Large Tumbler", "🧪")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Log Water Intake",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Choose your capsule size or custom quick logs below.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Preset row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            presets.take(2).forEach { cup ->
                CupPresetCard(cup = cup, onSelect = onCupSelected, modifier = Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Preset row 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            presets.drop(2).forEach { cup ->
                CupPresetCard(cup = cup, onSelect = onCupSelected, modifier = Modifier.weight(1f))
            }
        }

        // Custom containers inside bottom sheet
        if (customContainers.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Saved Containers",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(customContainers, key = { it.id }) { container ->
                        QuickLogChip(
                            name = container.name,
                            volume = container.volumeMl,
                            emoji = container.emoji,
                            onClick = { onCupSelected(container.volumeMl) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Custom Quick Input Button
        var customAmountStr by remember { mutableStateOf("") }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = customAmountStr,
                onValueChange = {
                    if (it.isEmpty() || (it.length <= 4 && it.all { char -> char.isDigit() })) {
                        customAmountStr = it
                    }
                },
                label = { Text("Custom Amount") },
                suffix = { Text("ml") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_custom_cup_amount")
            )

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = {
                    val amt = customAmountStr.toIntOrNull() ?: 0
                    if (amt > 0) {
                        onCupSelected(amt)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .height(56.dp)
                    .testTag("btn_log_custom_amount"),
                enabled = customAmountStr.isNotEmpty()
            ) {
                Text("Log")
            }
        }
    }
}

@Composable
fun WaterProgressRing(
    progress: Float,
    totalIntake: Int,
    modifier: Modifier = Modifier,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
    textColor: Color = MaterialTheme.colorScheme.onBackground
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 100f),
        label = "progress_ring_animation"
    )

    Box(
        modifier = modifier
            .size(190.dp)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw background track
            drawArc(
                color = trackColor,
                startAngle = -225f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw active filling progress arc
            drawArc(
                color = primaryColor,
                startAngle = -225f,
                sweepAngle = 270f * animatedProgress.coerceIn(0f, 1f),
                useCenter = false,
                style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Inside display info
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = String.format("%,d", totalIntake),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1).sp
                ),
                color = textColor
            )
            Text(
                text = "ml total",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = textColor.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun HydrationLogItem(
    log: WaterLog,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    val formattedTime = sdf.format(Date(log.timestamp))

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    WaterDropIcon(
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "${log.amountMl} ml",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete log",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun CupPresetCard(
    cup: PresetCup,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { onSelect(cup.amountMl) }
            .testTag("preset_cup_${cup.amountMl}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = cup.emoji,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = cup.label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )
            Text(
                text = "${cup.amountMl} ml",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

data class PresetCup(
    val amountMl: Int,
    val label: String,
    val emoji: String
)

fun formatMinutes(minutes: Int): String {
    val hrs = minutes / 60
    val m = minutes % 60
    return when {
        hrs > 0 && m > 0 -> "${hrs}h ${m}m"
        hrs > 0 -> "${hrs}h"
        else -> "${m}m"
    }
}
