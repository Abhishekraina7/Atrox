package com.example.atrox.ui.main.dashboard

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
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

// Theme Colors
private val ColorBackground = Color(0xFF0A0A0F)
private val ColorCard = Color(0xFF14141E)
private val ColorCardLighter = Color(0xFF1E1E2D)
private val ColorAccent = Color(0xFF6C63FF)
private val ColorAccentDim = Color(0xFF6C63FF).copy(alpha = 0.15f)
private val ColorTextPrimary = Color(0xFFFFFFFF)
private val ColorTextSecondary = Color(0xFF8888A0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val isPhoneBlockActive by viewModel.isPhoneBlockActive.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Good morning, Alex",
                            color = ColorTextPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp
                        )
                    }
                },
                actions = {
                    // Streak Badge
                    Row(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .background(ColorAccentDim, RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocalFireDepartment, 
                            contentDescription = null, 
                            tint = ColorAccent, 
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "12 DAYS", 
                            color = ColorAccent, 
                            fontSize = 12.sp, 
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                },
                windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ColorBackground
                ),
            )
        },
        containerColor = ColorBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorBackground)
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp, bottom = 32.dp)
        ) {
        // --- 2. Current Sprint Card ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ColorCard, RoundedCornerShape(16.dp))
                .padding(20.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "CURRENT SPRINT",
                        color = ColorAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Website Audit",
                        color = ColorTextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Sprint 2 of 4",
                        color = ColorTextSecondary,
                        fontSize = 14.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(ColorCardLighter, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Rounded.BarChart, contentDescription = null, tint = ColorTextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "PROGRESS", color = ColorTextSecondary, fontSize = 10.sp, letterSpacing = 1.sp)
                Text(text = "50%", color = ColorTextSecondary, fontSize = 10.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(ColorCardLighter, RoundedCornerShape(50))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(6.dp)
                        .background(ColorAccent, RoundedCornerShape(50))
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Start Focus Button
            Button(
                onClick = { /* TODO */ },
                colors = ButtonDefaults.buttonColors(containerColor = ColorAccent),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Icon(imageVector = Icons.Rounded.PlayArrow, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Focus", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 3. Stat Cards ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(title = "FOCUS", value = "4.5h", modifier = Modifier.weight(1f))
            StatCard(title = "TASKS", value = "12", modifier = Modifier.weight(1f))
            StatCard(title = "SAVED", value = "1.2h", modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 4. Phone Block ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ColorCard, RoundedCornerShape(16.dp))
                .border(1.dp, ColorAccentDim, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(ColorAccent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Rounded.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Phone Block", color = ColorTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(text = "Strict mode is currently active", color = ColorTextSecondary, fontSize = 12.sp)
            }
            Switch(
                checked = isPhoneBlockActive,
                onCheckedChange = { viewModel.togglePhoneBlock() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = ColorAccent,
                    uncheckedThumbColor = ColorTextSecondary,
                    uncheckedTrackColor = ColorCardLighter
                )
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- 5. Today's Tasks ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Today's Tasks", color = ColorTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = "VIEW ALL", color = ColorAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            tasks.forEach { task ->
                TaskItemRow(
                    task = task,
                    onToggle = { viewModel.toggleTaskCompletion(task.id) }
                )
            }
        }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(ColorCard, RoundedCornerShape(12.dp))
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, color = ColorTextSecondary, fontSize = 10.sp, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = value, color = ColorAccent, fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun TaskItemRow(task: TaskItem, onToggle: () -> Unit) {
    val tagColor = when(task.category) {
        "WORK" -> Color(0xFF4A5568)
        "DESIGN" -> Color(0xFF311B92)
        "ADMIN" -> Color(0xFF263238)
        else -> ColorCardLighter
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ColorCard, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (task.isCompleted) ColorAccent else Color.Transparent)
                .border(2.dp, if (task.isCompleted) ColorAccent else ColorTextSecondary, RoundedCornerShape(6.dp))
                .clickable { onToggle() },
            contentAlignment = Alignment.Center
        ) {
            if (task.isCompleted) {
                Icon(imageVector = Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
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
                Text(text = "${task.durationMin}m", color = ColorTextSecondary, fontSize = 10.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = task.title, 
                color = if (task.isCompleted) ColorTextSecondary else ColorTextPrimary, 
                fontSize = 15.sp,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
            )
        }

        Icon(imageVector = Icons.Rounded.MoreVert, contentDescription = null, tint = ColorTextSecondary)
    }
}
