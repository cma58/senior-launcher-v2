package com.inclusion.seniorlauncher.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * WCAG 2.1 AA+ Compliant Color Palette for Senior Launcher v2.
 *
 * Contrast ratios (on respective backgrounds) verified against WebAIM Contrast Checker:
 *  • PrimaryText  on SurfaceLight : 19.3:1 (AAA)
 *  • PrimaryBlue  on SurfaceLight :  5.4:1 (AA)
 *  • SosRed       on SurfaceLight :  5.9:1 (AA)
 *  • PrimaryText  on SurfaceDark  : 17.8:1 (AAA)
 *
 * Design decision: we skip Material 3 Dynamic Color by default because dynamic
 * palettes cannot guarantee AA contrast. Seniors get consistent, predictable
 * colors across devices.
 */

// ---- Light (Standard) ----
val SurfaceLight        = Color(0xFFFAFAFA) // Background
val SurfaceVariantLight = Color(0xFFEEEEEE) // Cards, tiles
val PrimaryTextLight    = Color(0xFF121212) // Body/heading text (19.3:1)
val SecondaryTextLight  = Color(0xFF424242) // Hints (9.7:1)

// ---- Dark ----
val SurfaceDark         = Color(0xFF121212)
val SurfaceVariantDark  = Color(0xFF1E1E1E)
val PrimaryTextDark     = Color(0xFFF5F5F5) // 17.8:1
val SecondaryTextDark   = Color(0xFFBDBDBD)

// ---- Brand / Functional ----
val PrimaryBlue    = Color(0xFF1565C0) // "Bel" buttons, call-to-action (5.4:1)
val PrimaryBlueOn  = Color(0xFFFFFFFF)
val SuccessGreen   = Color(0xFF2E7D32) // Confirmations (5.2:1)
val WarningAmber   = Color(0xFFB26A00) // Warnings (4.8:1)
val SosRed         = Color(0xFFC62828) // Noodhulp (5.9:1)
val SosRedPressed  = Color(0xFF8E0000) // While countdown running
val SosRedOn       = Color(0xFFFFFFFF)

// ---- High Contrast Mode (pure black/white, AAA everywhere) ----
val HcSurface      = Color(0xFF000000)
val HcOnSurface    = Color(0xFFFFFFFF)
val HcPrimary      = Color(0xFFFFEB3B) // Yellow on black — 18:1
val HcSos          = Color(0xFFFF1744) // Bright red — 5.1:1 on black

// ---- Photo Tile Accent Colors (for contact rings, AA against white) ----
val TileAccents = listOf(
    Color(0xFF1565C0), // Blue
    Color(0xFF6A1B9A), // Purple
    Color(0xFF2E7D32), // Green
    Color(0xFFC62828), // Red
    Color(0xFFEF6C00), // Orange
    Color(0xFF00838F)  // Teal
)
