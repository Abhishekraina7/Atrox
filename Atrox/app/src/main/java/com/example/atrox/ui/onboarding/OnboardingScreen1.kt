package com.example.atrox.ui.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.atrox.R
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

// Node positions defined as fractions of the canvas size
private data class NodeDef(val xFrac: Float, val yFrac: Float)
private data class EdgeDef(val from: Int, val to: Int)

private val nodes = listOf(
    NodeDef(0.50f, 0.18f),  // 0  top center
    NodeDef(0.22f, 0.30f),  // 1  upper left
    NodeDef(0.78f, 0.30f),  // 2  upper right
    NodeDef(0.35f, 0.50f),  // 3  mid left
    NodeDef(0.65f, 0.50f),  // 4  mid right
    NodeDef(0.50f, 0.42f),  // 5  center
    NodeDef(0.15f, 0.65f),  // 6  lower left
    NodeDef(0.50f, 0.70f),  // 7  lower center
    NodeDef(0.85f, 0.65f),  // 8  lower right
    NodeDef(0.35f, 0.85f),  // 9  bottom left
    NodeDef(0.65f, 0.85f),  // 10 bottom right
)

private val edges = listOf(
    EdgeDef(0, 1), EdgeDef(0, 2), EdgeDef(0, 5),
    EdgeDef(1, 3), EdgeDef(1, 5), EdgeDef(2, 4), EdgeDef(2, 5),
    EdgeDef(3, 5), EdgeDef(4, 5), EdgeDef(3, 6), EdgeDef(3, 7),
    EdgeDef(4, 7), EdgeDef(4, 8), EdgeDef(5, 7),
    EdgeDef(6, 9), EdgeDef(7, 9), EdgeDef(7, 10), EdgeDef(8, 10),
    EdgeDef(9, 10),
)

@Composable
fun NeuralNetworkCanvas(modifier: Modifier = Modifier) {
    val indigo = Color(0xFF6C63FF)
    val dimGray = Color(0xFF2A2A2A)

    // Staggered entrance animation for each node
    val nodeAnimations = remember { nodes.map { Animatable(0f) } }
    LaunchedEffect(Unit) {
        nodeAnimations.forEachIndexed { index, anim ->
            delay(index * 120L)
            anim.animateTo(1f, animationSpec = tween(600, easing = EaseInOutCubic))
        }
    }

    // Staggered entrance for edges (start after first few nodes)
    val edgeAnimations = remember { edges.map { Animatable(0f) } }
    LaunchedEffect(Unit) {
        delay(400)
        edgeAnimations.forEachIndexed { index, anim ->
            delay(index * 80L)
            anim.animateTo(1f, animationSpec = tween(500, easing = EaseInOutCubic))
        }
    }

    // Continuous pulse for the glow effect
    val infiniteTransition = rememberInfiniteTransition(label = "neural_pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Resolve node positions
        val nodePositions = nodes.map { Offset(it.xFrac * w, it.yFrac * h) }

        // Draw edges (connections)
        edges.forEachIndexed { index, edge ->
            val progress = edgeAnimations[index].value
            if (progress > 0f) {
                val from = nodePositions[edge.from]
                val to = nodePositions[edge.to]
                val currentEnd = Offset(
                    from.x + (to.x - from.x) * progress,
                    from.y + (to.y - from.y) * progress
                )
                // Dim base line
                drawLine(
                    color = dimGray.copy(alpha = 0.5f * progress),
                    start = from,
                    end = currentEnd,
                    strokeWidth = 1.5f,
                    cap = StrokeCap.Round
                )
                // Bright indigo overlay
                drawLine(
                    color = indigo.copy(alpha = 0.3f * progress * pulse),
                    start = from,
                    end = currentEnd,
                    strokeWidth = 2.5f,
                    cap = StrokeCap.Round
                )
            }
        }

        // Draw nodes
        nodePositions.forEachIndexed { index, pos ->
            val nodeProgress = nodeAnimations[index].value
            if (nodeProgress > 0f) {
                val nodeRadius = 6f * nodeProgress
                val glowRadius = 14f * nodeProgress

                // Outer glow
                drawCircle(
                    color = indigo.copy(alpha = 0.15f * pulse * nodeProgress),
                    radius = glowRadius,
                    center = pos
                )
                // Core node
                drawCircle(
                    color = indigo.copy(alpha = 0.7f + 0.3f * pulse),
                    radius = nodeRadius,
                    center = pos
                )
                // Bright center dot
                drawCircle(
                    color = Color.White.copy(alpha = 0.9f * nodeProgress),
                    radius = 2.5f * nodeProgress,
                    center = pos
                )
            }
        }
    }
}

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
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 3. Animated Neural Network Illustration //
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            NeuralNetworkCanvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- 4. Main Title ---
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onBackground)) { append(stringResource(R.string.onboarding1_title_part1)) }
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontStyle = FontStyle.Italic)) { append(stringResource(R.string.onboarding1_title_part2)) }
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
            text = stringResource(R.string.onboarding1_subtitle),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        // --- 7. Call To Action Button ---
        Button(
            onClick = { viewModel.onBeginClicked() },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(text = stringResource(R.string.onboarding1_begin_button), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(imageVector = Icons.Rounded.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}
