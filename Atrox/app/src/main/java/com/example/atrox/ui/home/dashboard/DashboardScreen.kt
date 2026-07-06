package com.example.atrox.ui.home.dashboard

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.AddTask
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.atrox.R
import com.example.atrox.data.tasks.TaskItem
import com.example.atrox.ui.main.dashboard.DashboardViewModel
import com.example.atrox.ui.theme.atroxColors
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onStartFocus: (taskId: String) -> Unit = {},
    onNavigateToAddTask: () -> Unit = {}
) {
    val isPhoneBlockActive by viewModel.isPhoneBlockActive.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val nextPendingTask by viewModel.nextPendingTask.collectAsState()
    val atroxColors = MaterialTheme.atroxColors
    val streakCount by viewModel.maxStreak.collectAsStateWithLifecycle()
    var showAllTasks by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    var showDndDisclosureDialog by remember { mutableStateOf(false) }
    var dndPermissionRequested by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && dndPermissionRequested) {
                dndPermissionRequested = false
                if (notificationManager.isNotificationPolicyAccessGranted) {
                    viewModel.togglePhoneBlock()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    AnimatedGreeting()
                },
                actions = {
                    // Streak Badge
                    Row(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .background(atroxColors.indigoDim, RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocalFireDepartment, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = streakCount.toString(),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp, 
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                },
                windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp, bottom = 32.dp)
        ) {
            // --- 2. Sprint Card (Dynamic) ---
            if (nextPendingTask != null) {
                CurrentSprintCard(task = nextPendingTask!!, onStartFocus = onStartFocus)
            } else {
                EmptySprintCard(onAddTaskClick = onNavigateToAddTask)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 3. Stat Cards ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(title = stringResource(R.string.dashboard_stat_focus), value = "4.5h", modifier = Modifier.weight(1f))
                StatCard(title = stringResource(R.string.dashboard_stat_tasks), value = "12", modifier = Modifier.weight(1f))
                StatCard(title = stringResource(R.string.dashboard_stat_saved), value = "1.2h", modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 4. Phone Block ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(atroxColors.cardDefault, RoundedCornerShape(16.dp))
                    .border(1.dp, atroxColors.indigoDim, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Rounded.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = stringResource(R.string.dashboard_phone_block_title), color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = stringResource(R.string.dashboard_phone_block_desc), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
                Switch(
                    checked = isPhoneBlockActive,
                    onCheckedChange = { 
                        if (!isPhoneBlockActive && !notificationManager.isNotificationPolicyAccessGranted) {
                            showDndDisclosureDialog = true
                        } else {
                            viewModel.togglePhoneBlock()
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        uncheckedTrackColor = atroxColors.cardElevated
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- 5. Today's Tasks ---
            val displayedTasks = if (showAllTasks) tasks else tasks.filter { !it.isCompleted }

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(R.string.dashboard_tasks_title), color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = if (showAllTasks) "View Less" else stringResource(R.string.dashboard_view_all), 
                    color = MaterialTheme.colorScheme.primary, 
                    fontSize = 12.sp, 
                    fontWeight = FontWeight.Bold, 
                    letterSpacing = 1.sp,
                    modifier = Modifier.clickable { showAllTasks = !showAllTasks }
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                displayedTasks.forEach { task ->
                    TaskItemRow(
                        task = task,
                        onToggle = { viewModel.toggleTaskCompletion(task.id) }
                    )
                }
                
                if (displayedTasks.isEmpty() && tasks.isNotEmpty() && !showAllTasks) {
                    Text(
                        text = "All tasks completed!", 
                        color = MaterialTheme.colorScheme.onSurfaceVariant, 
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                    )
                }
            }
        }
    }

    if (showDndDisclosureDialog) {
        AlertDialog(
            onDismissRequest = { showDndDisclosureDialog = false },
            title = { Text("Do Not Disturb Access Required", fontWeight = FontWeight.Bold) },
            text = { Text("To turn on Phone Block, Atrox needs access to your Do Not Disturb settings.\n\nPlease grant this permission on the next screen.") },
            confirmButton = {
                TextButton(onClick = {
                    showDndDisclosureDialog = false
                    dndPermissionRequested = true
                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
                }) {
                    Text("Grant Permission", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDndDisclosureDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            textContentColor = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    val atroxColors = MaterialTheme.atroxColors
    Column(
        modifier = modifier
            .background(atroxColors.cardDefault, RoundedCornerShape(12.dp))
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = value, color = MaterialTheme.colorScheme.primary, fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun TaskItemRow(task: TaskItem, onToggle: () -> Unit) {
    val atroxColors = MaterialTheme.atroxColors
    val tagColor = when(task.category) {
        "WORK" -> Color(0xFF4A5568)
        "DESIGN" -> Color(0xFF311B92)
        "ADMIN" -> Color(0xFF263238)
        else -> atroxColors.cardElevated
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(atroxColors.cardDefault, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (task.isCompleted) MaterialTheme.colorScheme.primary else Color.Transparent)
                .border(2.dp, if (task.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, RoundedCornerShape(6.dp))
                .clickable { onToggle() },
            contentAlignment = Alignment.Center
        ) {
            if (task.isCompleted) {
                Icon(imageVector = Icons.Rounded.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.background(tagColor, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = task.category, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "${task.durationMin}m", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = task.title, 
                color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onBackground,
                fontSize = 15.sp,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
            )
        }

        Icon(imageVector = Icons.Rounded.MoreVert, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ----------------------------
// Current Sprint Card
// ----------------------------
@Composable
fun CurrentSprintCard(
    task: TaskItem,
    onStartFocus: (taskId: String) -> Unit = {}
) {
    val atroxColors = MaterialTheme.atroxColors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(atroxColors.cardDefault, RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = stringResource(R.string.dashboard_current_sprint_label),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = task.title,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.dashboard_task_duration_desc, task.durationMin),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(atroxColors.cardElevated, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.BarChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        // Start Focus Button
        Button(
            onClick = { onStartFocus(task.id) },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Icon(imageVector = Icons.Rounded.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.dashboard_start_focus_button), color = MaterialTheme.colorScheme.onPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ----------------------------
// Empty Sprint Card
// ----------------------------
@Composable
fun EmptySprintCard(onAddTaskClick: () -> Unit = {}) {
    val atroxColors = MaterialTheme.atroxColors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(atroxColors.cardDefault, RoundedCornerShape(16.dp))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(MaterialTheme.colorScheme.background, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.AddTask,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.dashboard_empty_tasks_title),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.dashboard_empty_tasks_desc),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onAddTaskClick,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text(stringResource(R.string.dashboard_add_task_button), color = MaterialTheme.colorScheme.onPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

enum class TimeOfDay {
    MORNING, AFTERNOON, EVENING, NIGHT, LATE_NIGHT, EARLY_MORNING
}

@Composable
fun getGreetingAndTimeOfDay(): Pair<String, TimeOfDay> {
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    
    return when (hour) {
        in 5..11 -> "Good morning" to TimeOfDay.MORNING
        in 12..15 -> "Good afternoon" to TimeOfDay.AFTERNOON
        in 16..19 -> "Good evening" to TimeOfDay.EVENING
        in 20..22 -> "Night Time" to TimeOfDay.NIGHT
        23 -> "Going to Sleep?" to TimeOfDay.LATE_NIGHT
        else -> "Still awake?" to TimeOfDay.EARLY_MORNING
    }
}

@Composable
fun AnimatedGreeting() {
    val (greetingText, timeOfDay) = getGreetingAndTimeOfDay()
    
    val infiniteTransition = rememberInfiniteTransition(label = "GreetingAnimation")
    
    // Bobbing animation for the celestial body
    val offsetY by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "IconBobbing"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "IconRotation"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "IconPulse"
    )

    val iconColor = when (timeOfDay) {
        TimeOfDay.MORNING -> Color(0xFFFFB300) // Deep yellow
        TimeOfDay.AFTERNOON -> Color(0xFFFF9800) // Orange
        TimeOfDay.EVENING -> Color(0xFFFF5722) // Deep Orange
        TimeOfDay.NIGHT, TimeOfDay.LATE_NIGHT, TimeOfDay.EARLY_MORNING -> Color(0xFF9FA8DA) // Light Indigo
    }
    
    val bgColor = MaterialTheme.colorScheme.background

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier.size(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerOffset = Offset(size.width / 2, size.height / 2 + offsetY)
                val radius = size.width / 3.5f
                
                if (timeOfDay == TimeOfDay.MORNING || timeOfDay == TimeOfDay.AFTERNOON) {
                    // Rotating sun rays
                    rotate(rotation, pivot = centerOffset) {
                        for (i in 0 until 8) {
                            rotate(i * 45f, pivot = centerOffset) {
                                drawRoundRect(
                                    color = iconColor.copy(alpha = pulseAlpha),
                                    topLeft = Offset(centerOffset.x - radius * 0.15f, centerOffset.y - radius * 1.8f),
                                    size = Size(radius * 0.3f, radius * 0.6f),
                                    cornerRadius = CornerRadius(2f, 2f)
                                )
                            }
                        }
                    }
                    
                    // Sun core
                    drawCircle(
                        color = iconColor,
                        radius = radius,
                        center = centerOffset
                    )
                } else if (timeOfDay == TimeOfDay.EVENING) {
                    // Setting sun (half circle or slightly submerged)
                    drawCircle(
                        color = iconColor.copy(alpha = pulseAlpha),
                        radius = radius * 1.5f,
                        center = centerOffset.copy(y = centerOffset.y + radius * 0.5f)
                    )
                    drawCircle(
                        color = iconColor,
                        radius = radius,
                        center = centerOffset.copy(y = centerOffset.y + radius * 0.5f)
                    )
                    // Cutoff for horizon
                    drawRect(
                        color = bgColor,
                        topLeft = Offset(0f, centerOffset.y + radius * 1.2f),
                        size = Size(size.width, size.height)
                    )
                } else {
                    // Moon
                    drawCircle(
                        color = iconColor,
                        radius = radius,
                        center = centerOffset
                    )
                    // Moon inner shadow (crescent cutout)
                    drawCircle(
                        color = bgColor,
                        radius = radius * 0.85f,
                        center = centerOffset.copy(x = centerOffset.x - radius * 0.4f, y = centerOffset.y - radius * 0.2f)
                    )
                    
                    // Twinkling stars
                    val starCenter1 = Offset(centerOffset.x + radius * 1.5f, centerOffset.y - radius * 0.8f)
                    drawCircle(
                        color = iconColor.copy(alpha = pulseAlpha),
                        radius = radius * 0.2f,
                        center = starCenter1
                    )
                    val starCenter2 = Offset(centerOffset.x - radius * 1.2f, centerOffset.y + radius * 1.2f)
                    drawCircle(
                        color = iconColor.copy(alpha = (1f - pulseAlpha + 0.3f).coerceIn(0f, 1f)), // Opposite phase
                        radius = radius * 0.15f,
                        center = starCenter2
                    )
                }
            }
        }
        Text(
            text = greetingText,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.5).sp
        )
    }
}
