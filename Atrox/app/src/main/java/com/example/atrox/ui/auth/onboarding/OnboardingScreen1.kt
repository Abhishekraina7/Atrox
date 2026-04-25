package com.example.atrox.ui.auth.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

// Colors from Obsidian Focus Theme
private val ColorBackground = Color(0xFF0F0F16)
private val ColorAccent = Color(0xFF6C63FF)
private val ColorAccentVariant = Color(0xFF4A43D4)
private val ColorTextPrimary = Color(0xFFFFFFFF)
private val ColorTextSecondary = Color(0xFF8888A0)
private val ColorSurface = Color(0xFF14141E)
private val ColorSurfaceStroke = Color(0xFF2A2A3A)

@Composable
fun OnboardingScreen1(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onNavigateToNext: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when(event) {
                is OnboardingEvent.NavigateToNext -> onNavigateToNext()
                is OnboardingEvent.NavigateToLogin -> onNavigateToLogin()
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground)
            .padding(horizontal = 24.dp)
            .padding(top = 48.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. Progress Indicators ---
        Row(
            modifier = Modifier.padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(28.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(ColorAccent)
            )
            repeat(3) {
                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(ColorSurfaceStroke)
                )
            }
        }

        // --- 2. Stage Subtitle ---
        Text(
            text = "STAGE 01 — INITIATION",
            color = ColorAccentVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // --- 3. Image Graphic Container ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(1.dp, ColorSurfaceStroke, RoundedCornerShape(8.dp))
                .background(ColorSurface, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
        ) {
            // TODO: Crop your graphic, save as 'img_onboarding_1.png' in res/drawable, and uncomment!
            // Image(
            //     painter = painterResource(id = R.drawable.img_onboarding_1),
            //     contentDescription = "Onboarding Graphic",
            //     contentScale = ContentScale.Crop,
            //     modifier = Modifier.fillMaxSize()
            // )

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .border(1.dp, ColorSurfaceStroke, CircleShape)
                    .background(ColorBackground.copy(alpha = 0.8f), CircleShape)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "LVL. 00",
                    color = ColorAccentVariant,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- 4. Main Title ---
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = ColorTextPrimary)) { append("Train your focus\n") }
                withStyle(style = SpanStyle(color = ColorAccent, fontStyle = FontStyle.Italic)) { append("like a muscle.") }
            },
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            lineHeight = 36.sp,
            letterSpacing = (-1).sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // --- 5. Subtitle Text ---
        Text(
            text = "Atrox helps you regain control over your digital habits through scientific focus training and neuro-performance coaching.",
            color = ColorTextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            InfoChip(iconText = "⚡", text = "NEURAL_SYNC")
            InfoChip(iconText = "⏱\uFE0F", text = "21_DAY_PATH")
        }

        // --- 7. Call To Action Button ---
        Button(
            onClick = { viewModel.onBeginClicked() },
            colors = ButtonDefaults.buttonColors(containerColor = ColorAccent),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(text = "Let's begin", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(imageVector = Icons.Rounded.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 8. Footer Link ---
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = ColorTextSecondary)) { append("Already have an account? ") }
                withStyle(style = SpanStyle(color = ColorTextPrimary, fontWeight = FontWeight.SemiBold)) { append("Sign in") }
            },
            fontSize = 14.sp,
            modifier = Modifier
                .clickable { viewModel.onSignInClicked() }
                .padding(8.dp)
        )
    }
}

@Composable
fun InfoChip(iconText: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .border(1.dp, ColorSurfaceStroke, RoundedCornerShape(8.dp))
            .background(ColorSurface, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = iconText, fontSize = 10.sp)
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, color = ColorTextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.sp)
    }
}
