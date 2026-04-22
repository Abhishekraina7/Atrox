package com.example.atrox.ui.auth.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.atrox.app.ui.theme.DmMono
import com.atrox.app.ui.theme.PlusJakartaSans
import com.example.atrox.ui.theme.AtroxTheme
import com.example.atrox.ui.theme.Background
import com.example.atrox.ui.theme.IndigoAccent
import com.example.atrox.ui.theme.IndigoSoft
import com.example.atrox.ui.theme.TextPrimary
import com.example.atrox.ui.theme.TextSecondary

// Definition of the Obsidian Focus Theme Colors
private val ColorLogoDarkDot = Color(0xFF3A2FD6)

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = hiltViewModel(), // Injects the ViewModel via Hilt
    // These callbacks are handled by your parent Navigation Graph
    onNavigateToOnboarding: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
) {
    // Collect the 1-shot navigation events from the ViewModel
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SplashEvent.NavigateToOnboarding -> onNavigateToOnboarding()
                is SplashEvent.NavigateToLogin -> onNavigateToLogin()
                is SplashEvent.NavigateToHome -> onNavigateToHome()
            }
        }
    }
    SplashScreenContent()
}

@Composable
fun SplashScreenContent() {
    // State for the progress bar animation at the bottom
    var startProgressAnim by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (startProgressAnim) 0.3f else 0f,
        animationSpec = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
        label = "progress_anim"
    )

    // Trigger the animation upon launch
    LaunchedEffect(Unit) {
        startProgressAnim = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // ZONE 3: BACKGROUND
        // Draws the subtle radial indigo glow right behind the center elements
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val radius = canvasWidth * 0.8f
            
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(IndigoAccent.copy(alpha = 0.08f), Color.Transparent),
                    center = Offset(canvasWidth / 2, canvasHeight / 2),
                    radius = radius
                ),
                radius = radius,
                center = Offset(canvasWidth / 2, canvasHeight / 2)
            )
        }
        // ZONE 1: CENTER Layout
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LogoSection()
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // "Atrox" Wordmark
            Text(
                text = "Atrox",
                color = TextPrimary,
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-2).sp,
                fontFamily = PlusJakartaSans 
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Tagline below the wordmark
            Text(
                text = "TRAIN YOUR FOCUS LIKE A MUSCLE",
                color = IndigoAccent,
                fontSize = 10.sp,
                letterSpacing = 3.sp,
                fontFamily = DmMono // TODO: Include DM Mono
            )
        }

        // ZONE 2: BOTTOM Layout
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .padding(bottom = 52.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CALIBRATING ENVIRONMENT",
                    color = TextSecondary,
                    fontSize = 9.sp,
                    fontFamily = DmMono
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    color = IndigoSoft,
                    fontSize = 9.sp,
                    fontFamily = DmMono
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Progress Track
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
            ) {
                // Background Track (7% white fill)
                drawLine(
                    color = Color.White.copy(alpha = 0.07f),
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = size.height,
                    cap = StrokeCap.Round
                )
                
                // Active Track (Indigo fill animating 0 -> 30%)
                drawLine(
                    color = IndigoAccent,
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width * progress, size.height / 2),
                    strokeWidth = size.height,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
fun LogoSection() {
    // Endless transition for the "Breathing" effect
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400), // 1400ms scale up + 1400ms down = 2800ms loop
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_anim"
    )

    Box(
        modifier = Modifier.size(160.dp),
        contentAlignment = Alignment.Center
    ) {
        // Pulsing Indigo Ring
        Canvas(modifier = Modifier.fillMaxSize().scale(scale)) {
            drawCircle(
                color = IndigoAccent,
                radius = size.width / 2,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // Inner Square Logo (80dp, corner radius 20dp)
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(IndigoAccent),
            contentAlignment = Alignment.Center
        ) {
            // Inner White circle
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                // Center Dark dot
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(ColorLogoDarkDot)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    AtroxTheme {
        SplashScreenContent()
    }
}
