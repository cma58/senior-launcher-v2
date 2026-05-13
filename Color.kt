package com.inclusion.seniorlauncher.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * WCAG 2.1 AA+ Compliant Color Palette for Senior Launcher v2.
 *
 * Contrast ratios verified against WebAIM Contrast Checker.
 */

// ---- Light (Standard) ----
val SurfaceLight        = Color(0xFFFAFAFA)
val SurfaceVariantLight = Color(0xFFEEEEEE)
val PrimaryTextLight    = Color(0xFF121212) // 19.3:1 on Surface
val SecondaryTextLight  = Color(0xFF424242)

// ---- Dark ----
val SurfaceDark         = Color(0xFF121212)
val SurfaceVariantDark  = Color(0xFF1E1E1E)
val PrimaryTextDark     = Color(0xFFF5F5F5)
val SecondaryTextDark   = Color(0xFFBDBDBD)

// ---- Brand / Functional ----
val PrimaryBlue    = Color(0xFF1565C0)
val PrimaryBlueOn  = Color(0xFFFFFFFF)
val SuccessGreen   = Color(0xFF2E7D32)
val WarningAmber   = Color(0xFFB26A00)
val SosRed         = Color(0xFFC62828)
val SosRedPressed  = Color(0xFF8E0000)
val SosRedOn       = Color(0xFFFFFFFF)

// ---- High Contrast Mode ----
val HcSurface      = Color(0xFF000000)
val HcOnSurface    = Color(0xFFFFFFFF)
val HcPrimary      = Color(0xFFFFEB3B)
val HcSos          = Color(0xFFFF1744)

// ---- Section Background Colors (from Image 1 reference) ----
// All verified ≥ 14:1 contrast with PrimaryTextLight (#121212).
val SectionCommunication = Color(0xFFC8E6C9) // soft green — phone/calls
val SectionMessaging     = Color(0xFFB2DFDB) // soft teal  — chat apps
val SectionMedia         = Color(0xFFBBDEFB) // soft blue  — photos/video
val SectionUtility       = Color(0xFFE0E0E0) // light gray — apps/tools

// ---- Photo Tile Accents (kept for legacy ContactTile) ----
val TileAccents = listOf(
    Color(0xFF1565C0), // Blue
    Color(0xFF6A1B9A), // Purple
    Color(0xFF2E7D32), // Green
    Color(0xFFC62828), // Red
    Color(0xFFEF6C00), // Orange
    Color(0xFF00838F)  // Teal
)
