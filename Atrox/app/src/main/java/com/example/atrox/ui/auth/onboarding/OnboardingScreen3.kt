package com.example.atrox.ui.auth.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.atrox.ui.theme.atroxColors

@Composable
fun OnboardingScreen3(
    viewModel: Onboarding3ViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToNext: () -> Unit,
    onNavigateToSkip: () -> Unit
) {
    val sprintDuration by viewModel.sprintDuration.collectAsState()
    val breakDuration by viewModel.breakDuration.collectAsState()
    val dailySprints by viewModel.dailySprints.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is Onboarding3Event.NavigateBack -> onNavigateBack()
                is Onboarding3Event.NavigateToNext -> onNavigateToNext()
                is Onboarding3Event.NavigateToSkip -> onNavigateToSkip()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 48.dp, bottom = 32.dp)
    ) {
        // --- 1. Top Bar ---
        Box(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(24.dp)
                    .clickable { viewModel.onBackClicked() }
            )

            Text(
                text = "Sprint Preferences",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // --- 2. Title Section ---
        Text(
            text = "Optimize your rhythm",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 32.sp,
            letterSpacing = (-1).sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        )
        Text(
            text = "Set your focus and recovery intervals for maximum productivity.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 40.dp)
        )

        // --- 4. Sprint Duration ---
        SectionHeader(icon = Icons.Rounded.Timer, title = "SPRINT DURATION")
        SegmentedControl(
            options = listOf(15, 25, 45, 60),
            selectedValue = sprintDuration,
            onValueSelected = { viewModel.onSprintDurationSelected(it) },
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // --- 5. Break Duration ---
        SectionHeader(icon = Icons.Rounded.LocalCafe, title = "BREAK DURATION")
        SegmentedControl(
            options = listOf(5, 10, 15),
            selectedValue = breakDuration,
            onValueSelected = { viewModel.onBreakDurationSelected(it) },
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // --- 6. Daily Sprints Goal ---
        SectionHeader(icon = Icons.Rounded.DateRange, title = "DAILY SPRINTS GOAL")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Minus Button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.atroxColors.cardElevated)
                    .clickable { viewModel.onDecrementSprints() },
                contentAlignment = Alignment.Center
            ) {
                Text("-", color = MaterialTheme.colorScheme.onBackground, fontSize = 24.sp)
            }

            // Center Info
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$dailySprints",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "SPRINTS / DAY",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }

            // Plus Button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.atroxColors.cardElevated)
                    .clickable { viewModel.onIncrementSprints() },
                contentAlignment = Alignment.Center
            ) {
                Text("+", color = MaterialTheme.colorScheme.onBackground, fontSize = 24.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        // --- 7. Total Focus Time ---
        val totalFocusTime = sprintDuration * dailySprints
        Text(
            text = "TOTAL FOCUS TIME: $totalFocusTime MIN / DAY",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 40.dp)
        )

        // --- 8. Action Buttons ---
        Spacer(modifier = Modifier.weight(1f, fill = false)) 

        Button(
            onClick = { viewModel.onContinueClicked() },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(text = "Continue", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(imageVector = Icons.Rounded.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "I'll do this later",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.onSkipClicked() }
                .padding(8.dp)
        )
    }
}

@Composable
fun SectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun SegmentedControl(
    options: List<Int>,
    selectedValue: Int,
    onValueSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
    ) {
        options.forEach { value ->
            val isSelected = value == selectedValue
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onValueSelected(value) }
                    .padding(vertical = 12.dp)
            ) {
                Text(
                    text = "$value",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 18.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = "MIN",
                    color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}