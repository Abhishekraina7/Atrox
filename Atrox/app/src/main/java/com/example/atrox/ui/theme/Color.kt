package com.example.atrox.ui.theme

import androidx.compose.ui.graphics.Color

// ─── Obsidian Focus · Core Backgrounds ───────────────────────────────────────
val Background       = Color(0xFF0A0A0A)   // Matte Black — page bg
val Surface          = Color(0xFF111111)   // Void — nav bar, bottom sheets
val CardDefault      = Color(0xFF181818)   // Obsidian — default cards
val CardElevated     = Color(0xFF202020)   // Lifted — modal surfaces

// ─── Accent ───────────────────────────────────────────────────────────────────
val IndigoAccent     = Color(0xFF6C63FF)   // Electric Indigo — primary CTA
val IndigoSoft       = Color(0xFF8B84FF)   // Indigo Tint — labels, icons
val IndigoDim        = Color(0x246C63FF)   // 14% — badge fill
val IndigoBorder     = Color(0x4D6C63FF)   // 30% — card borders, outlines

// ─── Text ─────────────────────────────────────────────────────────────────────
val TextPrimary      = Color(0xFFF0F0F0)   // Off White — headlines, body
val TextSecondary    = Color(0xFF8888A0)   // Muted — subtitles, hints
val TextTertiary     = Color(0xFF383848)   // Ghost — disabled, placeholders

// ─── Borders ──────────────────────────────────────────────────────────────────
val BorderSubtle     = Color(0x0FFFFFFF)   // 6% — card edges
val BorderDefault    = Color(0x1AFFFFFF)   // 10% — separator lines
val BorderAccent     = Color(0x4D6C63FF)   // 30% — accent card borders

// ─── Semantic ─────────────────────────────────────────────────────────────────
val ErrorRed         = Color(0xFFFF5252)   // Destructive actions
val ErrorRedDim      = Color(0x24FF5252)   // 14% — error badge fill
val SuccessGreen     = Color(0xFF4ADE80)   // Active/completed states
val WarningAmber     = Color(0xFFF59E0B)   // Streak / warning

// ─── Material 3 Role Mapping ──────────────────────────────────────────────────
// Dark scheme — used inside darkColorScheme()
val md_primary              = IndigoAccent
val md_onPrimary            = Color(0xFFFFFFFF)
val md_primaryContainer     = Color(0xFF2D2880)
val md_onPrimaryContainer   = Color(0xFFE0DEFF)
val md_secondary            = IndigoSoft
val md_onSecondary          = Color(0xFF1A1A2E)
val md_secondaryContainer   = Color(0xFF1E1B40)
val md_onSecondaryContainer = Color(0xFFCDCAFF)
val md_background           = Background
val md_onBackground         = TextPrimary
val md_surface              = Surface
val md_onSurface            = TextPrimary
val md_surfaceVariant       = CardDefault
val md_onSurfaceVariant     = TextSecondary
val md_outline              = BorderDefault
val md_outlineVariant       = BorderSubtle
val md_error                = ErrorRed
val md_onError              = Color(0xFFFFFFFF)
val md_errorContainer       = ErrorRedDim
val md_onErrorContainer     = Color(0xFFFFDAD6)
val md_inverseSurface       = Color(0xFFF0F0F0)
val md_inverseOnSurface     = Background
val md_inversePrimary       = Color(0xFF4A42CC)
val md_scrim                = Color(0xFF000000)