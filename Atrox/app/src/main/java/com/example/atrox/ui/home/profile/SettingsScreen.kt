package com.example.atrox.ui.home.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.hilt.navigation.compose.hiltViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val colors = MaterialTheme.colorScheme

    var showSprintDurationDialog by remember { mutableStateOf(false) }
    var showBreakDurationDialog by remember { mutableStateOf(false) }
    var showDailySprintGoalDialog by remember { mutableStateOf(false) }
    
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
                    viewModel.toggleBlockNotifications(true)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "SETTINGS",
                        color = colors.onBackground,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.onBackground
                        )
                    }
                },
                // Add a placeholder action to center the title properly
                actions = {
                    Spacer(modifier = Modifier.width(48.dp))
                },
                windowInsets = WindowInsets(0,0,0,0),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // ── FOCUS PREFERENCES ──
            SettingsHeader("FOCUS PREFERENCES")
            SettingsCard {
                SettingsNavRow(
                    title = "Default sprint duration",
                    subtitle = "Standard Pomodoro",
                    value = "${uiState.sprintDuration} min",
                    onClick = { showSprintDurationDialog = true }
                )
                SettingsNavRow(
                    title = "Default break duration",
                    subtitle = "Short interval",
                    value = "${uiState.breakDuration} min",
                    onClick = { showBreakDurationDialog = true }
                )
                SettingsNavRow(
                    title = "Daily sprint goal",
                    subtitle = null,
                    value = "${uiState.dailySprintGoal} sprints",
                    onClick = { showDailySprintGoalDialog = true }
                )
                SettingsSwitchRow(
                    title = "Auto-start next sprint",
                    checked = uiState.autoStartNextSprint,
                    onCheckedChange = { viewModel.toggleAutoStartNextSprint() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── PHONE RESTRICTION ──
            SettingsHeader("PHONE RESTRICTION")
            SettingsCard {
                SettingsSwitchRow(
                    title = "Block notifications",
                    checked = uiState.blockNotifications,
                    onCheckedChange = { checked ->
                        if (checked) {
                            if (notificationManager.isNotificationPolicyAccessGranted) {
                                viewModel.toggleBlockNotifications(true)
                            } else {
                                showDndDisclosureDialog = true
                            }
                        } else {
                            viewModel.toggleBlockNotifications(false)
                        }
                    }
                )
                SettingsSwitchRow(
                    title = "Strict break time",
                    checked = uiState.blockSocialApps,
                    onCheckedChange = { viewModel.toggleBlockSocialApps() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── REGULATOR ──
            SettingsHeader("REGULATOR")
            SettingsCard {
                SettingsNavRow(
                    title = "Configure Regulator",
                    icon = Icons.Rounded.OpenInNew,
                    onClick = { /* TODO */ }
                )
                SettingsSwitchRow(
                    title = "Approval for early exit",
                    subtitle = "Require passcode to stop session",
                    checked = uiState.approvalForEarlyExit,
                    onCheckedChange = { viewModel.toggleApprovalForEarlyExit() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── NOTIFICATIONS ──
            SettingsHeader("NOTIFICATIONS")
            SettingsCard {
                SettingsSwitchRow(
                    title = "Sprint reminders",
                    checked = uiState.sprintReminders,
                    onCheckedChange = { viewModel.toggleSprintReminders() }
                )
                SettingsSwitchRow(
                    title = "Daily goal nudge",
                    checked = uiState.dailyGoalNudge,
                    onCheckedChange = { viewModel.toggleDailyGoalNudge() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── ACCOUNT ──
            SettingsHeader("ACCOUNT")
            SettingsCard {
                SettingsNavRow(
                    title = "Edit profile",
                    onClick = { /* TODO */ }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── APP ──
            SettingsHeader("APP")
            SettingsCard {
                SettingsNavRow(
                    title = "Theme",
                    value = uiState.theme,
                    valueColor = colors.onSurfaceVariant,
                    icon = null,
                    onClick = { /* TODO */ }
                )
                SettingsSwitchRow(
                    title = "Haptic feedback",
                    checked = uiState.hapticFeedback,
                    onCheckedChange = { viewModel.toggleHapticFeedback() }
                )
                SettingsNavRow(
                    title = "App version",
                    value = uiState.appVersion,
                    valueColor = colors.onSurfaceVariant.copy(alpha = 0.5f),
                    icon = null,
                    onClick = { /* TODO */ }
                )

                HorizontalDivider(
                    color = colors.onSurfaceVariant.copy(alpha = 0.1f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Text(
                    text = "LOGOUT",
                    color = colors.error,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { /* TODO */ }
                        .padding(vertical = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        if (showSprintDurationDialog) {
            SettingInputDialog(
                title = "Sprint Duration (mins)",
                initialValue = uiState.sprintDuration.toString(),
                onDismiss = { showSprintDurationDialog = false },
                onSave = { viewModel.updateSprintDuration(it) }
            )
        }
        if (showBreakDurationDialog) {
            SettingInputDialog(
                title = "Break Duration (mins)",
                initialValue = uiState.breakDuration.toString(),
                onDismiss = { showBreakDurationDialog = false },
                onSave = { viewModel.updateBreakDuration(it) }
            )
        }
        if (showDailySprintGoalDialog) {
            SettingInputDialog(
                title = "Daily Sprint Goal",
                initialValue = uiState.dailySprintGoal.toString(),
                onDismiss = { showDailySprintGoalDialog = false },
                onSave = { viewModel.updateDailySprintGoal(it) }
            )
        }
        if (showDndDisclosureDialog) {
            AlertDialog(
                onDismissRequest = { showDndDisclosureDialog = false },
                title = { Text("Do Not Disturb Access Required", fontWeight = FontWeight.Bold) },
                text = { Text("To automatically block notifications and silence distractions during a Focus Session (while still allowing important calls), Atrox needs access to your Do Not Disturb settings.\n\nPlease grant this permission on the next screen.") },
                confirmButton = {
                    TextButton(onClick = {
                        showDndDisclosureDialog = false
                        dndPermissionRequested = true
                        context.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
                    }) {
                        Text("Grant Permission", color = colors.primary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDndDisclosureDialog = false }) {
                        Text("Cancel", color = colors.onSurfaceVariant)
                    }
                },
                containerColor = colors.surfaceVariant,
                titleContentColor = colors.onSurfaceVariant,
                textContentColor = colors.onBackground
            )
        }
    }
}

@Composable
private fun SettingsHeader(title: String) {
    val colors = MaterialTheme.colorScheme
    Text(
        text = title,
        color = colors.primary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            content = content
        )
    }
}

@Composable
private fun SettingsNavRow(
    title: String,
    subtitle: String? = null,
    value: String? = null,
    valueColor: Color = MaterialTheme.colorScheme.primary,
    icon: ImageVector? = Icons.Rounded.ChevronRight,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = colors.onBackground, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle, 
                    color = colors.onSurfaceVariant, 
                    fontSize = 12.sp, 
                    fontFamily = FontFamily.Monospace
                )
            }
        }
        if (value != null) {
            Text(text = value, color = valueColor, fontSize = 13.sp)
            if (icon != null) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
        if (icon != null) {
            Icon(
                imageVector = icon, 
                contentDescription = null, 
                tint = colors.primary, 
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = colors.onBackground, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle, 
                    color = colors.onSurfaceVariant, 
                    fontSize = 12.sp, 
                    fontFamily = FontFamily.Monospace
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = colors.primary,
                uncheckedThumbColor = colors.onSurfaceVariant,
                uncheckedTrackColor = colors.surface,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

@Composable
fun SettingInputDialog(
    title: String,
    initialValue: String,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    var value by remember { mutableStateOf(initialValue) }
    val colors = MaterialTheme.colorScheme

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceVariant,
        title = { Text(text = title, color = colors.onSurfaceVariant, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() }) value = it },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.onSurfaceVariant.copy(alpha = 0.5f),
                    focusedTextColor = colors.onBackground,
                    unfocusedTextColor = colors.onBackground
                )
            )
        },
        confirmButton = {
            TextButton(onClick = { 
                val intVal = value.toIntOrNull()
                if (intVal != null && intVal > 0) {
                    onSave(intVal)
                    onDismiss()
                }
            }) {
                Text("Save", color = colors.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colors.onSurfaceVariant)
            }
        }
    )
}
