package com.example.atrox.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.atrox.domain.model.AppBadge
import kotlinx.coroutines.delay

@Composable
fun GlobalBadgePopup(
    isBlocked: Boolean,
    viewModel: GlobalBadgeViewModel = hiltViewModel()
) {
    val pendingBadges = remember { mutableStateListOf<AppBadge>() }
    var currentlyDisplayingBadge by remember { mutableStateOf<AppBadge?>(null) }
    
    LaunchedEffect(Unit) {
        viewModel.newlyUnlockedBadgeEvent.collect { badge ->
            pendingBadges.add(badge)
        }
    }

    LaunchedEffect(isBlocked, pendingBadges.size, currentlyDisplayingBadge) {
        if (!isBlocked && currentlyDisplayingBadge == null && pendingBadges.isNotEmpty()) {
            currentlyDisplayingBadge = pendingBadges.removeAt(0)
            // Auto dismiss after 4 seconds
            delay(4000)
            currentlyDisplayingBadge = null
        }
    }

    currentlyDisplayingBadge?.let { badge ->
        Dialog(onDismissRequest = { currentlyDisplayingBadge = null }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Badge Unlocked!",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(badge.color.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = badge.emoji, fontSize = 40.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = badge.title,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = badge.description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { currentlyDisplayingBadge = null },
                        colors = ButtonDefaults.buttonColors(containerColor = badge.color),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Awesome!", color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
