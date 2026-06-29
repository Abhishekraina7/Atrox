package com.example.atrox.ui.home.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.atrox.ui.theme.atroxColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreakHistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: StreakHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = MaterialTheme.colorScheme
    val extendedColors = MaterialTheme.atroxColors
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Streak History",
                        color = colors.onBackground,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
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
                windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
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

            // ── Current Focus Streak Card ────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, extendedColors.cardElevated, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "CURRENT FOCUS STREAK",
                        color = colors.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "${uiState.currentStreak}",
                            color = colors.onBackground,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 48.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Days",
                            color = colors.onSurfaceVariant,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Consistency Card (Half Width) ───────────
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .border(1.dp, extendedColors.cardElevated, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "CONSISTENCY",
                        color = colors.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${uiState.consistency}%",
                        color = colors.onBackground,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 36.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // ── Calendar History Header ──────────────────
            Text(
                text = "Calendar History",
                color = colors.onBackground,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Calendar Month List ──────────────────────
            uiState.calendarHistory.forEach { month ->
                MonthCalendarCard(month = month)
                Spacer(modifier = Modifier.height(20.dp))
            }

            OutlinedButton(
                onClick = { viewModel.loadMoreMonths() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primary),
                border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
            ) {
                Text(text = "View More", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }


    }
}

@Composable
fun MonthCalendarCard(month: MonthHistory) {
    val colors = MaterialTheme.colorScheme
    val extendedColors = MaterialTheme.atroxColors

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, extendedColors.cardElevated, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            // Month title & badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = month.monthName,
                    color = colors.onBackground,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .background(colors.onSurfaceVariant.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = month.activeCountText,
                        color = colors.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Weekday labels
            val weekdays = listOf("S", "M", "T", "W", "T", "F", "S")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weekdays.forEach { day ->
                    Text(
                        text = day,
                        color = colors.onSurfaceVariant.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Grid of days
            // Build the slots (empty slots at start + actual days)
            val slots = mutableListOf<DayHistory?>()
            repeat(month.startingDayOfWeek - 1) {
                slots.add(null)
            }
            month.days.forEach { slots.add(it) }

            // Chunk slots into rows of 7
            val rows = slots.chunked(7)
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (i in 0 until 7) {
                            val day = row.getOrNull(i)
                            Box(
                                modifier = Modifier
                                    .size(36.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (day != null) {
                                    when (day.status) {
                                        DayStatus.ACTIVE -> {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(colors.primary),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${day.dayNumber}",
                                                    color = Color.White,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        DayStatus.TODAY -> {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .border(2.dp, colors.primary, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${day.dayNumber}",
                                                    color = colors.onBackground,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        DayStatus.INACTIVE -> {
                                            Text(
                                                text = "${day.dayNumber}",
                                                color = colors.onSurfaceVariant.copy(alpha = 0.4f),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Normal
                                            )
                                        }
                                        else -> {}
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
