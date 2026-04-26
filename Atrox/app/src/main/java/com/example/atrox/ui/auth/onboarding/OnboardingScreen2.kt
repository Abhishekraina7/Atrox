package com.example.atrox.ui.auth.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

private val ColorBackground = Color(0xFF0F0F16)
private val ColorAccent = Color(0xFF6C63FF)
private val ColorSurface = Color(0xFF14141E)
private val ColorSurfaceLight = Color(0xFF1C1C28)
private val ColorSurfaceStroke = Color(0xFF2A2A3A)
private val ColorTextPrimary = Color(0xFFFFFFFF)
private val ColorTextSecondary = Color(0xFF8888A0)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen2(
    viewModel: Onboarding2ViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToNext: () -> Unit,
    onNavigateToSkip: () -> Unit
) {
    val selectedGoal by viewModel.selectedGoal.collectAsState()
    val targetHours by viewModel.targetHours.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is Onboarding2Event.NavigateBack -> onNavigateBack()
                is Onboarding2Event.NavigateToNext -> onNavigateToNext()
                is Onboarding2Event.NavigateToSkip -> onNavigateToSkip()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 48.dp, bottom = 32.dp)
    ) {
        // --- 1. Top Bar (Back Button + Progress) ---
        Box(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = ColorTextSecondary,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(24.dp)
                    .clickable { viewModel.onBackClicked() }
            )
        }

        // --- 2. Title Section ---
        Text(
            text = "What do you want to achieve?",
            color = ColorTextPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 36.sp,
            letterSpacing = (-1).sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Text(
            text = "Select your primary focus goal to personalize your experience.",
            color = ColorTextSecondary,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // --- 3. Goals FlowRow ---
        val goals = listOf(
            "Deep Work" to "🚀",
            "Study" to "📖",
            "Creative Work" to "🎨",
            "Reading" to "📚",
            "Coding" to "💻"
        )
        
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(bottom = 40.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            goals.forEach { (text, emoji) ->
                val isSelected = selectedGoal == text
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) ColorAccent.copy(alpha = 0.15f) else Color.Transparent)
                        .border(
                            1.dp,
                            if (isSelected) ColorAccent else ColorSurfaceStroke,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.onGoalSelected(text) }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(text = emoji, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = text,
                        color = if (isSelected) ColorTextPrimary else ColorTextSecondary,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }

        // --- 4. Daily Focus Target Slider ---
        Text(
            text = "Daily focus target",
            color = ColorTextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${targetHours.toInt()}",
                    color = ColorAccent,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 48.sp
                )
                Text(
                    text = "h",
                    color = ColorTextSecondary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                )
            }
            
            // RECOMMENDED Badge
            Box(
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .background(ColorAccent.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "RECOMMENDED",
                    color = ColorAccent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        Slider(
            value = targetHours,
            onValueChange = { viewModel.onHoursChanged(it) },
            valueRange = 1f..8f,
            steps = 6,
            colors = SliderDefaults.colors(
                thumbColor = ColorAccent,
                activeTrackColor = ColorAccent,
                inactiveTrackColor = ColorSurfaceStroke,
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            (1..8).forEach { h ->
                Text(
                    text = "${h}h",
                    color = ColorTextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        // --- 5. Info Banner ---
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ColorAccent.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                .background(ColorSurfaceLight, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(ColorAccent, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("⚡", fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Productivity Boost",
                    color = ColorTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Setting a daily target increases your consistency by 40% based on user data.",
                    color = ColorTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 6. Action Buttons ---
        Button(
            onClick = { viewModel.onContinueClicked() },
            colors = ButtonDefaults.buttonColors(containerColor = ColorAccent),
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
            color = ColorTextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.onSkipClicked() }
                .padding(8.dp)
        )
    }
}
