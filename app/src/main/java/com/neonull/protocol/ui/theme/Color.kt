package com.neonull.protocol.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * NEO-NULL: PYTHON LEARNING PROTOCOL
 * Industrial Matrix palette: restrained phosphor accents on layered dark surfaces.
 */
object NeoColors {
    // Layered dark surfaces
    val ObsidianVoid = Color(0xFF080C0F)
    val DeepNavyBlack = Color(0xFF0B0F12)
    val TerminalSurface = Color(0xFF111820)
    val ElevatedPanel = Color(0xFF172129)
    val GutterSurface = Color(0xFF0D1419)
    val DeckGridLine = Color(0xFF1D2A31)
    val BorderSubtle = Color(0xFF34434A)

    // Restrained functional accents
    val MatrixGreen = Color(0xFF65E6A5)
    val MatrixGreenDim = Color(0x3365E6A5)
    val CyberCyan = Color(0xFF74DCE8)
    val CyberCyanDim = Color(0x2E74DCE8)

    // Critical alerts, exploit action and OVERLORD identity
    val CrimsonRed = Color(0xFFFF858B)
    val CrimsonRedPressed = Color(0xFFE56D76)
    val CrimsonRedGlow = Color(0x40FF858B)
    val CrimsonDarkTint = Color(0xFF241519)

    // Syntax highlighting tokens
    val SyntaxKeyword = Color(0xFF74DCE8)      // def, if, else, return, for, class, import
    val SyntaxControl = Color(0xFF65E6A5)      // builtins, self, True/False, decorators
    val SyntaxString = Color(0xFFFFD27A)       // "...", '...', f-strings
    val SyntaxComment = Color(0xFF7C8982)      // # comments
    val SyntaxNumber = Color(0xFFB6C8F0)       // numeric literals
    val SyntaxAlert = Color(0xFFFF858B)        // errors & OVERLORD identifiers

    // Text hierarchy
    val TextPrimary = Color(0xFFD7E0DA)
    val TextSecondary = Color(0xFFA7B3AC)
    val TextMuted = Color(0xFF7C8982)
}
