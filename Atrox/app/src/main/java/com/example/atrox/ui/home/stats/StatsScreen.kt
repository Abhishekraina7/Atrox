package com.example.atrox.ui.home.stats

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.YearMonth
import java.time.format.TextStyle
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.atrox.ui.home.profile.CatalogueBadgeCard
import com.example.atrox.ui.theme.atroxColors
import androidx.compose.ui.platform.LocalLocale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: FocusViewModel = hiltViewModel(),
    onNavigateToProfile: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedDateTasks by viewModel.selectedDateTasks.collectAsState()
    val completedTaskDates by viewModel.completedTaskDates.collectAsState()
    var selectedDateForPopup by remember { mutableStateOf<String?>(null) }
    
    val scrollState = rememberScrollState()
    val atroxColors = MaterialTheme.atroxColors

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Activity",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    uiState.avatar?.gradientColors ?: listOf(Color(0xFFD4A574), Color(0xFFA67C52))
                                )
                            )
                            .clickable { onNavigateToProfile() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.avatar?.emoji ?: uiState.avatarInitial, 
                            color = Color.White, 
                            fontWeight = FontWeight.Bold, 
                            fontSize = if (uiState.avatar != null) 18.sp else 14.sp
                        )
                    }
                },
                windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
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
            
            // ── Weekly Performance ─────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Weekly\nPerformance",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 32.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Consistency is your superpower.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .background(atroxColors.cardElevated, RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "MAR 18-24",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize =10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PerformanceCard(
                    title = "TOTAL",
                    value = uiState.weeklyTotalHours.toString(),
                    modifier = Modifier.weight(1f),
                    hasGlow = true
                )
                PerformanceCard(
                    title = "AVERAGE",
                    value = uiState.weeklyAverageHours.toString(),
                    modifier = Modifier.weight(1f)
                )
                PerformanceCard(
                    title = "BEST",
                    value = uiState.weeklyBestHours.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            WeeklyFocusChart()
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // ── Streak & Consistency ───────────────
            Text(
                text = "Streak & Consistency",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF8A2BE2), Color(0xFF4B0082))
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CURRENT STREAK",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = uiState.currentStreak.toString(),
                        color = Color.White,
                        fontSize = 64.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "DAYS FOCUSED",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "LONGEST STREAK",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${uiState.longestStreak} Days",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "CONSISTENCY SCORE",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${uiState.consistencyScore}%",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            CalendarView(
                completedTaskDates = completedTaskDates,
                onDayClicked = { dateStr ->
                    viewModel.fetchTasksForDate(dateStr)
                    selectedDateForPopup = dateStr
                }
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // ── Milestones ─────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Milestones",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.unlockedBadges) { badgeState ->
                    // Reusing the CatalogueBadgeCard but wrapped to enforce a specific width
                    Box(modifier = Modifier.width(140.dp)) {
                        CatalogueBadgeCard(badgeState)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // ── Digital Balance ────────────────────
            Text(
                text = "Digital Balance",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "Daily Insight",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Your focus vs. screen time today.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                ) {
                    Box(modifier = Modifier.weight(0.7f).fillMaxHeight().background(Color(0xFFC0B3FF)))
                    Box(modifier = Modifier.weight(0.3f).fillMaxHeight().background(MaterialTheme.colorScheme.primary))
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(Color(0xFFC0B3FF), CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "FOCUS WORK",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(text = "${uiState.todayFocusWorkHours}h", color = MaterialTheme.colorScheme.onBackground, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "${uiState.todayFocusWorkMinutes}m", color = MaterialTheme.colorScheme.onBackground, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PHONE USE",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(text = "${uiState.todayPhoneUseHours}h", color = MaterialTheme.colorScheme.onBackground, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "${uiState.todayPhoneUseMinutes}m", color = MaterialTheme.colorScheme.onBackground, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(atroxColors.cardElevated, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("💡", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = uiState.insightText,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (selectedDateForPopup != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedDateForPopup = null },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Completed Tasks",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = selectedDateForPopup ?: "",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                if (selectedDateTasks.isEmpty()) {
                    Text(
                        text = "No completed tasks for this day.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                } else {
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(selectedDateTasks) { task ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp))
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${task.durationMin}m",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = task.title,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 16.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = "Completed",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun PerformanceCard(title: String, value: String, modifier: Modifier = Modifier, hasGlow: Boolean = false) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .border(
                1.dp, 
                if (hasGlow) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.Transparent, 
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = "h",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarView(completedTaskDates: Set<String>, onDayClicked: (String) -> Unit) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    
    val monthName = currentMonth.month.getDisplayName(TextStyle.FULL, LocalLocale.current.platformLocale)
    val year = currentMonth.year
    
    // Generate days
    val firstDayOfMonth = currentMonth.atDay(1)
    val startDayOfWeek = firstDayOfMonth.dayOfWeek.value // 1 (Mon) - 7 (Sun)
    val offset = startDayOfWeek - 1 // 0 for Mon, 6 for Sun
    
    val lengthOfMonth = currentMonth.lengthOfMonth()
    val prevMonth = currentMonth.minusMonths(1)
    val prevMonthLength = prevMonth.lengthOfMonth()
    
    val gridDays = mutableListOf<Triple<Int, Boolean, Boolean>>() // Day, IsCurrentMonth, IsFocused
    
    // Previous month padding
    for (i in offset downTo 1) {
        gridDays.add(Triple(prevMonthLength - i + 1, false, false))
    }
    
    // Current month days
    for (i in 1..lengthOfMonth) {
        val dateStr = String.format(java.util.Locale.US, "%04d-%02d-%02d", year, currentMonth.monthValue, i)
        val isFocused = completedTaskDates.contains(dateStr)
        gridDays.add(Triple(i, true, isFocused))
    }
    
    // Next month padding to complete rows
    val remaining = (7 - gridDays.size % 7) % 7
    for (i in 1..remaining) {
        gridDays.add(Triple(i, false, false))
    }
    
    // Determine number of weeks
    val weeks = gridDays.size / 7
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(24.dp))
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$monthName $year",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.ChevronLeft, 
                    contentDescription = "Previous Month", 
                    tint = if (currentMonth.monthValue > 1) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(enabled = currentMonth.monthValue > 1) {
                            currentMonth = currentMonth.minusMonths(1)
                        }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Rounded.ChevronRight, 
                    contentDescription = "Next Month", 
                    tint = if (currentMonth.monthValue < 12) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(enabled = currentMonth.monthValue < 12) {
                            currentMonth = currentMonth.plusMonths(1)
                        }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                Text(
                    text = day,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        for (week in 0 until weeks) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (dayIdx in 0..6) {
                    val index = week * 7 + dayIdx
                    if (index < gridDays.size) {
                        val (day, isCurrentMonth, isFocused) = gridDays[index]
                        
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isFocused && isCurrentMonth) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent
                                )
                                .clickable(enabled = isCurrentMonth) {
                                    val dateStr = String.format(java.util.Locale.US, "%04d-%02d-%02d", year, currentMonth.monthValue, day)
                                    onDayClicked(dateStr)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.toString(),
                                color = if (isFocused && isCurrentMonth) MaterialTheme.colorScheme.primary 
                                        else if (isCurrentMonth) MaterialTheme.colorScheme.onBackground 
                                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                fontSize = 12.sp,
                                fontWeight = if (isFocused && isCurrentMonth) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(32.dp))
                    }
                }
            }
            if (week < weeks - 1) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun WeeklyFocusChart() {
    val days = listOf("M", "T", "W", "T", "F", "S", "S")
    // Creating historical dummy data for horizontal scrolling demonstration
    val pastWeek2 = listOf(2.5f, 3.0f, 5.0f, 4.5f, 2.0f, 1.0f, 4.0f)
    val pastWeek1 = listOf(4.0f, 5.5f, 3.5f, 6.0f, 7.5f, 3.0f, 4.5f)
    val currentWeek = listOf(5.2f, 6.5f, 4.0f, 7.1f, 8.4f, 2.0f, 3.5f)
    
    val allWeeks = listOf(pastWeek2, pastWeek1, currentWeek)
    val maxValue = 8.4f
    val atroxColors = MaterialTheme.atroxColors
    
    var selectedWeekIndex by remember { mutableStateOf(2) }
    var selectedDayIndex by remember { mutableStateOf(4) }

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = 2)
    val visibleWeekIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    
    val weekLabel = when (allWeeks.size - 1 - visibleWeekIndex) {
        0 -> "This Week"
        1 -> "Last Week"
        else -> "${allWeeks.size - 1 - visibleWeekIndex} Weeks Ago"
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Focus Time",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = weekLabel,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth().height(140.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(allWeeks.size) { weekIndex ->
                val weekValues = allWeeks[weekIndex]
                Row(
                    modifier = Modifier.fillParentMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    weekValues.forEachIndexed { dayIndex, value ->
                        val isSelected = selectedWeekIndex == weekIndex && selectedDayIndex == dayIndex
                        val fillFraction = value / maxValue
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier
                                .fillMaxHeight()
                                .clickable {
                                    selectedWeekIndex = weekIndex
                                    selectedDayIndex = dayIndex
                                }
                        ) {
                            // Spacer takes the remaining weight so the texts are never distorted/pushed out of bounds
                            Spacer(modifier = Modifier.weight(1f - fillFraction + 0.02f))
                            
                            // Value Text
                            Text(
                                text = value.toString(),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            
                            // Bar
                            Box(
                                modifier = Modifier
                                    .width(28.dp)
                                    .weight(fillFraction + 0.02f)
                                    .background(
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else atroxColors.cardElevated,
                                        shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = days[dayIndex],
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
