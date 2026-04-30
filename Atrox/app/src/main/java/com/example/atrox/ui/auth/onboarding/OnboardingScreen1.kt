package com.example.atrox.ui.auth.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
import com.example.atrox.ui.theme.atroxColors
import com.example.atrox.R

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
            .padding(top = 48.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 3. Image Graphic Container ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
        ) {
            // TODO: Crop your graphic, save as 'img_onboarding_1.png' in res/drawable, and uncomment!
             Image(
                 painter = painterResource(id = R.drawable.img_onboarding_1),
                 contentDescription = "Onboarding Graphic",
                 contentScale = ContentScale.Crop,
                 modifier = Modifier.fillMaxSize()
             )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- 4. Main Title ---
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onBackground)) { append("Train your focus\n") }
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontStyle = FontStyle.Italic)) { append("like a muscle.") }
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
            Text(text = "Let's begin", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(imageVector = Icons.Rounded.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 8. Footer Link ---
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) { append("Already have an account? ") }
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)) { append("Sign in") }
            },
            fontSize = 14.sp,
            modifier = Modifier
                .clickable { viewModel.onSignInClicked() }
                .padding(8.dp)
        )
    }
}
