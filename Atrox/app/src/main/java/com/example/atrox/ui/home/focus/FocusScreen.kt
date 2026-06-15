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

@Composable
fun FocusScreen(
    onNavigateBack: () -> Unit,
    viewModel: FocusViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    // Auto-start when screen opens
    LaunchedEffect(uiState.task) {
        if (uiState.task != null && uiState.timerState == TimerState.IDLE) {
            viewModel.startTimer()
        }
    }

    // Auto-navigate back when approved
    LaunchedEffect(uiState.isApproved) {
        if (uiState.isApproved) {
            Toast.makeText(context, "Sprint canceled by Guardian", Toast.LENGTH_SHORT).show()
            onNavigateBack()
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = uiState.progressFraction,
        animationSpec = tween(durationMillis = 800),
        label = "timer_arc"
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            uiState.guardianPhone?.let { phone ->
                val data = workDataOf(
                    SendSmsWorker.KEY_PHONE_NUMBER to phone,
                    SendSmsWorker.KEY_MESSAGE to "Marcus is requesting to cancel their current focus sprint. Reply APPROVE to allow."
                )
                val request = OneTimeWorkRequestBuilder<SendSmsWorker>()
                    .setInputData(data)
                    .build()
                WorkManager.getInstance(context).enqueue(request)
                viewModel.markRequestSent()
            } ?: run {
                Toast.makeText(context, "No Guardian Phone found", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "SMS Permission is required to send the request.", Toast.LENGTH_SHORT).show()
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
                        text = "SPRINT 1  ·  ${uiState.task?.durationMin ?: 0} MIN",
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
                            contentDescription = "Stats",
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
                text = uiState.task?.title ?: "Loading...",
                color = ColorTextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "FOCUSING DEEPLY",
                color = ColorTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Timer Controls ───────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset
                IconButton(
                    onClick = { viewModel.resetTimer() },
                    modifier = Modifier
                        .size(48.dp)
                        .background(ColorCard, CircleShape)
                ) {
                    Icon(imageVector = Icons.Rounded.Refresh, contentDescription = "Reset", tint = ColorTextSecondary)
                }

                // Play / Pause
                IconButton(
                    onClick = {
                        if (uiState.timerState == TimerState.RUNNING) viewModel.pauseTimer()
                        else viewModel.startTimer()
                    },
                    modifier = Modifier
                        .size(64.dp)
                        .background(ColorAccent, CircleShape)
                ) {
                    Icon(
                        imageVector = if (uiState.timerState == TimerState.RUNNING) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }

                // Stats placeholder
                IconButton(
                    onClick = { },
                    modifier = Modifier
                        .size(48.dp)
                        .background(ColorCard, CircleShape)
                ) {
                    Icon(imageVector = Icons.Rounded.BarChart, contentDescription = "Stats", tint = ColorTextSecondary)
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }

        // ── Regulator Bottom Panel ────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    color = ColorCard,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            // Drag handle
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(40.dp)
                    .height(4.dp)
                    .background(ColorCardLighter, RoundedCornerShape(50))
            )

            Spacer(modifier = Modifier.height(24.dp))

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
                        text = "Ping your Regulator?",
                        color = ColorTextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Need to exit early? Notify Marcus.",
                        color = ColorTextSecondary,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { 
                    if (!uiState.isCancelRequestSent) {
                        // First check for Notification Access
                        val enabledListeners = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
                        if (enabledListeners == null || !enabledListeners.contains(context.packageName)) {
                            Toast.makeText(context, "Please grant Notification Access to Atrox first.", Toast.LENGTH_LONG).show()
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
                    Text("Waiting for approval...", color = ColorTextSecondary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                } else {
                    Icon(imageVector = Icons.Rounded.Send, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Send Request", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
