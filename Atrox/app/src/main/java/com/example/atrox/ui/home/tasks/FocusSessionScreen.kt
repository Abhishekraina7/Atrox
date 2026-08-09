package com.example.atrox.ui.home.tasks

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.atrox.ui.theme.atroxColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusSessionScreen(
    onNavigateBack: () -> Unit,
    onSessionFinished: () -> Unit = onNavigateBack,
    onNavigateToFocusBreak: () -> Unit,
    onNavigateToDashboard: () -> Unit = {},
    viewModel: FocusSessionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val atroxColors = MaterialTheme.atroxColors

    var showExitSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(uiState.isFinished) {
        if (uiState.isFinished) {
            if (showExitSheet) {
                sheetState.hide()
                showExitSheet = false
            }
            if (uiState.navigateToDashboard) {
                onNavigateToDashboard()
            } else {
                onSessionFinished()
            }
        }
    }

    LaunchedEffect(uiState.approvalMessage) {
        if (uiState.approvalMessage.isNotEmpty()) {
            android.widget.Toast.makeText(context, uiState.approvalMessage, android.widget.Toast.LENGTH_LONG).show()
        }
    }

    BackHandler(enabled = true) {
        // Do nothing to prevent back navigation
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { },
                actions = {
                    IconButton(
                        onClick = onNavigateToFocusBreak,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    ){
                        Icon(
                            imageVector = Icons.Rounded.GraphicEq, // Waveform placeholder
                            contentDescription = "Waveform",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                windowInsets = WindowInsets(0,0,0,0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Circular Timer
            val totalSeconds = uiState.durationMin * 60
            val progress = if (totalSeconds > 0) uiState.remainingSeconds.toFloat() / totalSeconds.toFloat() else 0f
            
            Box(
                modifier = Modifier
                    .size(280.dp),
                contentAlignment = Alignment.Center
            ) {
                val animatedProgress by animateFloatAsState(
                    targetValue = progress,
                    animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
                    label = "progress"
                )
                
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Background track
                    drawCircle(
                        color = Color.White.copy(alpha = 0.05f),
                        style = Stroke(width = 12.dp.toPx())
                    )
                    
                    // Foreground track
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(Color(0xFF8A2BE2), Color(0xFFC0B3FF), Color(0xFF8A2BE2))
                        ),
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Timer text
                val minutes = uiState.remainingSeconds / 60
                val seconds = uiState.remainingSeconds % 60
                val timeString = String.format("%02d:%02d", minutes, seconds)
                
                Text(
                    text = timeString,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Light,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Task Info
            Text(
                text = uiState.taskName,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "FOCUSING DEEPLY",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape))
                Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape))
                Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape))
            }

            Spacer(modifier = Modifier.weight(1f))

            // Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { viewModel.togglePause() },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = if (uiState.isPaused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                        contentDescription = if (uiState.isPaused) "Resume" else "Pause"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (uiState.isPaused) "Resume" else "Pause", fontWeight = FontWeight.Bold)
                }
                
                Button(
                    onClick = { 
                        if (uiState.hapticFeedbackEnabled) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        if (uiState.requireApproval) {
                            showExitSheet = true
                        } else {
                            viewModel.endSessionEarly()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.Stop, contentDescription = "End Session")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("End Session", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showExitSheet) {
        ModalBottomSheet(
            onDismissRequest = { showExitSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .padding(bottom = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar Placeholder
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color(0xFFD4A574), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(uiState.regulatorName.first().toString().uppercase(), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        // Online indicator
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = (-2).dp, y = (-2).dp)
                                .size(14.dp)
                                .background(Color(0xFF4CAF50), CircleShape)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            text = "Ping your Regulator?",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Need to exit early? Notify ${uiState.regulatorName}.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = {
                        if (uiState.hapticFeedbackEnabled) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        if (!uiState.isWaitingForApproval) {
                            viewModel.sendExitRequest()
                        }
                    },
                    enabled = !uiState.isWaitingForApproval,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (!uiState.isWaitingForApproval) {
                        Icon(imageVector = Icons.Rounded.Send, contentDescription = "Send Request", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (uiState.isWaitingForApproval) "Waiting for Approval..." else "Send Request", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (uiState.isRequestRejected) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.clearRejection() },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Red Circle Icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Rounded.Close,
                        contentDescription = "Rejected",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Request Rejected",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Your Regulator has denied your request to\nexit early.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Penalty Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "PENALTY ACTIVE",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "05:00",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Rounded.Warning,
                                contentDescription = "Warning",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ending now will break your streak and requires a\n5-minute wait.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        viewModel.clearRejection()
                        showExitSheet = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6C63FF), // Match the purple button from screenshot
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Return to Session", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        viewModel.clearRejection()
                        showExitSheet = false
                        viewModel.endSessionEarly()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("End Anyway", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
