package com.neonull.protocol.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neonull.protocol.R
import com.neonull.protocol.localization.AppLanguage
import com.neonull.protocol.ui.theme.NeoColors

/**
 * Original procedural Jetpack Compose HUD container with chamfered cyberpunk corners
 * and Crimson Red / Cyber Cyan border accents (no external copyrighted sprite sheets).
 */
@Composable
fun CyberFramePanel(
    modifier: Modifier = Modifier,
    borderColor: Color = NeoColors.CyberCyan,
    fillColor: Color = NeoColors.TerminalSurface,
    chamferDp: Dp = 12.dp,
    contentPadding: Dp = 14.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val c = chamferDp.toPx()
            val w = size.width
            val h = size.height

            val path = Path().apply {
                moveTo(c, 0f)
                lineTo(w - 4f, 0f)
                lineTo(w, 4f)
                lineTo(w, h - c)
                lineTo(w - c, h)
                lineTo(4f, h)
                lineTo(0f, h - 4f)
                lineTo(0f, c)
                close()
            }

            drawPath(path = path, color = fillColor)
            drawPath(
                path = path,
                color = borderColor.copy(alpha = 0.72f),
                style = Stroke(width = 1.4.dp.toPx())
            )

            // Tactical corner tick accents
            val tick = 10.dp.toPx()
            drawLine(
                color = borderColor,
                start = Offset(0f, c),
                end = Offset(0f, c + tick),
                strokeWidth = 2.4.dp.toPx()
            )
            drawLine(
                color = borderColor,
                start = Offset(w - c, h),
                end = Offset(w - c - tick, h),
                strokeWidth = 2.4.dp.toPx()
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            content = content
        )
    }
}

@Composable
fun LanguageSwitchRow(
    selected: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppLanguage.entries.forEach { lang ->
            val isSelected = lang == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (isSelected) NeoColors.MatrixGreenDim else NeoColors.ElevatedPanel
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) NeoColors.MatrixGreen else NeoColors.BorderSubtle,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable { onSelect(lang) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = lang.code,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (isSelected) NeoColors.MatrixGreen else NeoColors.TextSecondary
                )
            }
        }
    }
}

@Composable
fun GlitchMentorPanel(
    title: String,
    dialogue: String,
    isWarning: Boolean,
    isBossStage: Boolean,
    warningBannerText: String,
    modifier: Modifier = Modifier
) {
    val activeBorder = if (isWarning || isBossStage) NeoColors.CrimsonRed else NeoColors.CyberCyan
    val activeFill = if (isWarning) NeoColors.CrimsonDarkTint else NeoColors.TerminalSurface

    CyberFramePanel(
        modifier = modifier.fillMaxWidth(),
        borderColor = activeBorder,
        fillColor = activeFill
    ) {
        Column {
            if (isWarning || isBossStage) {
                Text(
                    text = "ALERT · $warningBannerText",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = NeoColors.CrimsonRed
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(
                        id = if (isBossStage && isWarning) {
                            R.drawable.overlord_boss_core
                        } else {
                            R.drawable.glitch_mentor_avatar
                        }
                    ),
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, activeBorder, RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isWarning) NeoColors.CrimsonRed else NeoColors.CyberCyan
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = dialogue,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = NeoColors.TextPrimary,
                            lineHeight = 19.sp
                        )
                    )
                }
            }
        }
    }
}
