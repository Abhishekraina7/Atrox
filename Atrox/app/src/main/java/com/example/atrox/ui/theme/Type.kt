package com.atrox.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.atrox.app.R

// ─── Font Families ────────────────────────────────────────────────────────────
// Add fonts to res/font/ folder:
//   plus_jakarta_sans_regular.ttf   → weight 400
//   plus_jakarta_sans_medium.ttf    → weight 500
//   plus_jakarta_sans_semibold.ttf  → weight 600
//   plus_jakarta_sans_bold.ttf      → weight 700
//   plus_jakarta_sans_extrabold.ttf → weight 800
//   dm_mono_regular.ttf             → weight 400
//   dm_mono_medium.ttf              → weight 500

val PlusJakartaSans = FontFamily(
    Font(R.font.plus_jakarta_sans_regular,   FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_medium,    FontWeight.Medium),
    Font(R.font.plus_jakarta_sans_semibold,  FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans_bold,      FontWeight.Bold),
    Font(R.font.plus_jakarta_sans_extrabold, FontWeight.ExtraBold),
)

val DmMono = FontFamily(
    Font(R.font.dm_mono_regular, FontWeight.Normal),
    Font(R.font.dm_mono_medium,  FontWeight.Medium),
)

// ─── Typography Scale ─────────────────────────────────────────────────────────
// Convention:
//   PlusJakartaSans → all UI text (headings, body, buttons, labels)
//   DmMono         → timers, numeric stats, category tags, mono labels

val AtroxTypography = Typography(

    // ── Display (splash / streak hero) ──
    displayLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.ExtraBold,
        fontSize   = 52.sp,
        lineHeight = 56.sp,
        letterSpacing = (-2).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.ExtraBold,
        fontSize   = 40.sp,
        lineHeight = 44.sp,
        letterSpacing = (-1.5).sp,
    ),
    displaySmall = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize   = 32.sp,
        lineHeight = 36.sp,
        letterSpacing = (-1).sp,
    ),

    // ── Headline (screen titles, section headers) ──
    headlineLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.ExtraBold,
        fontSize   = 28.sp,
        lineHeight = 32.sp,
        letterSpacing = (-1).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize   = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.5).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize   = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.3).sp,
    ),

    // ── Title (card titles, list item labels) ──
    titleLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize   = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.2).sp,
    ),
    titleMedium = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.1).sp,
    ),
    titleSmall = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 12.sp,
        lineHeight = 18.sp,
    ),

    // ── Body ──
    bodyLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Normal,
        fontSize   = 15.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Normal,
        fontSize   = 13.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Normal,
        fontSize   = 12.sp,
        lineHeight = 18.sp,
    ),

    // ── Label (buttons, nav tabs, ghost buttons) ──
    labelLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.1).sp,
    ),
    labelMedium = TextStyle(
        fontFamily = DmMono,              // category tags, section caps
        fontWeight = FontWeight.Normal,
        fontSize   = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 2.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = DmMono,              // nav labels, tiny mono text
        fontWeight = FontWeight.Normal,
        fontSize   = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.5.sp,
    ),
)

// ─── Convenience Extensions ───────────────────────────────────────────────────
// Use these for DM Mono timer / stats text in Compose:
//   Text("25:00", style = AtroxTypography.timerDisplay)
//   Text("2.4h", style = AtroxTypography.statValue)

val TimerDisplay = TextStyle(
    fontFamily    = DmMono,
    fontWeight    = FontWeight.Medium,
    fontSize      = 64.sp,
    lineHeight    = 64.sp,
    letterSpacing = (-1).sp,
)

val StatValue = TextStyle(
    fontFamily    = DmMono,
    fontWeight    = FontWeight.Medium,
    fontSize      = 24.sp,
    lineHeight    = 28.sp,
    letterSpacing = (-0.5).sp,
)

val MonoTag = TextStyle(
    fontFamily    = DmMono,
    fontWeight    = FontWeight.Normal,
    fontSize      = 10.sp,
    lineHeight    = 14.sp,
    letterSpacing = 2.sp,
)