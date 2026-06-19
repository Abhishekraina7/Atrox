package com.example.atrox.ui.home.focus

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.atrox.R
import com.example.atrox.service.worker.SendSmsWorker
import android.content.Intent
import android.provider.Settings

// Theme Colors
private val ColorBackground = Color(0xFF0A0A0F)
private val ColorCard = Color(0xFF14141E)
private val ColorCardLighter = Color(0xFF1E1E2D)
private val ColorAccent = Color(0xFF6C63FF)
private val ColorTextPrimary = Color(0xFFFFFFFF)
private val ColorTextSecondary = Color(0xFF8888A0)
private val ColorTrack = Color(0xFF1E1E2D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusScreen(
    onNavigateBack: () -> Unit,
    viewModel: FocusViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    // Auto-start when screen opens
    LaunchedEffect(uiState.task) {
        if (uiState.task != null && uiState.timerState == TimerState.IDLE) {
            viewModel.startTimer()
        }
    }

    // Auto-navigate back when approved
    val canceledMsg = stringResource(R.string.focus_toast_canceled)
    LaunchedEffect(uiState.isApproved) {
        if (uiState.isApproved) {
            Toast.makeText(context, canceledMsg, Toast.LENGTH_SHORT).show()
            onNavigateBack()
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = uiState.progressFraction,
        animationSpec = tween(durationMillis = 800),
        label = "timer_arc"
    )

    val smsRequestMsg = stringResource(R.string.focus_sms_request_message)
    val noGuardianMsg = stringResource(R.string.focus_toast_no_guardian)
    val smsPermissionMsg = stringResource(R.string.focus_toast_sms_permission)

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            uiState.guardianPhone?.let { phone ->
                val data = workDataOf(
                    SendSmsWorker.KEY_PHONE_NUMBER to phone,
                    SendSmsWorker.KEY_MESSAGE to smsRequestMsg
                )
                val request = OneTimeWorkRequestBuilder<SendSmsWorker>()
                    .setInputData(data)
                    .build()
                WorkManager.getInstance(context).enqueue(request)
                viewModel.markRequestSent()
            } ?: run {
                Toast.makeText(context, noGuardianMsg, Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, smsPermissionMsg, Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Top Bar ──────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        tint = ColorAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.focus_header_sprint, uiState.task?.durationMin ?: 0),
                        color = ColorTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(ColorCard, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = {},//TODO Open the music selection Screen
                        modifier = Modifier.size(40.dp)
                    ){
                        Icon(
                            imageVector = Icons.Outlined.PlayCircleOutline,
                            contentDescription = stringResource(R.string.focus_icon_stats_desc),
                            tint = ColorTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                }
            }

            // ── Circular Timer ────────────────────────
            Spacer(modifier = Modifier.weight(1f))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(280.dp)
                    .drawBehind {
                        val strokeWidth = 14.dp.toPx()
                        val diameter = size.minDimension - strokeWidth
                        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                        val arcSize = Size(diameter, diameter)

                        // Track (background ring)
                        drawArc(
                            color = ColorTrack,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        // Progress arc
                        drawArc(
                            color = ColorAccent,
                            startAngle = -90f,
                            sweepAngle = 360f * animatedProgress,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
            ) {
                Text(
                    text = viewModel.formatTime(uiState.remainingSeconds),
                    color = ColorTextPrimary,
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // ── Task Info ─────────────────────────────
            Text(
                text = uiState.task?.title ?: stringResource(R.string.focus_loading_task),
                color = ColorTextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.focus_status_text),
                color = ColorTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Timer Controls ───────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play / Pause
                Button(
                    onClick = {
                        if (uiState.timerState == TimerState.RUNNING) viewModel.pauseTimer()
                        else viewModel.startTimer()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorCardLighter),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    val isRunning = uiState.timerState == TimerState.RUNNING
                    Icon(
                        imageVector = if (isRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = stringResource(R.string.focus_button_play_pause),
                        tint = ColorTextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isRunning) "Pause" else "Resume",
                        color = ColorTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // End Session
                Button(
                    onClick = { showBottomSheet = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)), // Red color
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Text(
                        text = "End session",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }

        // ── Regulator Bottom Panel ────────────────────
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = ColorCard,
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(top = 16.dp, bottom = 8.dp)
                            .width(40.dp)
                            .height(4.dp)
                            .background(ColorCardLighter, RoundedCornerShape(50))
                    )
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .padding(bottom = 24.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Guardian avatar
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(Color(0xFFD4A574), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("M", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            // Online dot
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .background(Color(0xFF4CAF50), CircleShape)
                                    .align(Alignment.BottomEnd)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.focus_panel_title),
                                color = ColorTextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.focus_panel_desc),
                                color = ColorTextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    val notifAccessMsg = stringResource(R.string.focus_toast_notif_access)
                    Button(
                        onClick = { 
                            if (!uiState.isCancelRequestSent) {
                                // First check for Notification Access
                                val enabledListeners = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
                                if (enabledListeners == null || !enabledListeners.contains(context.packageName)) {
                                    Toast.makeText(context, notifAccessMsg, Toast.LENGTH_LONG).show()
                                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                    context.startActivity(intent)
                                } else {
                                    // If granted, proceed to ask for SMS permission to send the request
                                    permissionLauncher.launch(Manifest.permission.SEND_SMS)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.isCancelRequestSent) ColorCardLighter else ColorAccent
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        if (uiState.isCancelRequestSent) {
                            Text(stringResource(R.string.focus_waiting_approval), color = ColorTextSecondary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(imageVector = Icons.Rounded.Send, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(stringResource(R.string.focus_button_send_request), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
