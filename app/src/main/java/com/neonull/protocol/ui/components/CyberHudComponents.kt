package com.neonull.protocol.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
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
import com.neonull.protocol.viewmodel.RamVariableSlot
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

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

/**
 * Animated Compose Canvas visualization of the 7-Node SYNTHEX Neural Grid.
 * Supports flowing data packet pulses, rotating AI OVERLORD shield ring, and tap-to-open navigation.
 */
@Composable
fun TacticalNetworkRadar(
    completedIds: Set<Int>,
    activeModuleId: Int,
    highestUnlockedId: Int,
    isAlert: Boolean,
    onNodeClick: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val coords = listOf(
        0.10f to 0.55f,
        0.23f to 0.28f,
        0.37f to 0.70f,
        0.51f to 0.32f,
        0.65f to 0.68f,
        0.78f to 0.34f,
        0.90f to 0.50f
    )

    val transition = rememberInfiniteTransition(label = "radarAnim")
    val packetPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "packetPhase"
    )
    val sweepAngle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweepAngle"
    )
    val pulseRadius by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseRadius"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(118.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(NeoColors.ObsidianVoid)
            .border(
                1.dp,
                if (isAlert) NeoColors.CrimsonRed.copy(alpha = 0.75f) else NeoColors.BorderSubtle,
                RoundedCornerShape(8.dp)
            )
            .pointerInput(highestUnlockedId, completedIds, onNodeClick) {
                if (onNodeClick == null) return@pointerInput
                detectTapGestures { tapOffset ->
                    val w = size.width.toFloat()
                    val h = size.height.toFloat()
                    val hitRadius = 26.dp.toPx()
                    coords.forEachIndexed { idx, pair ->
                        val nodeId = idx + 1
                        val cx = pair.first * w
                        val cy = pair.second * h
                        if (hypot(tapOffset.x - cx, tapOffset.y - cy) <= hitRadius) {
                            if (nodeId <= highestUnlockedId || nodeId in completedIds) {
                                onNodeClick(nodeId)
                            }
                        }
                    }
                }
            }
    ) {
        val w = size.width
        val h = size.height

        // Background telemetry grid lines
        val gridCols = 8
        for (c in 1 until gridCols) {
            val gx = (w / gridCols) * c
            drawLine(
                color = NeoColors.BorderSubtle.copy(alpha = 0.35f),
                start = Offset(gx, 0f),
                end = Offset(gx, h),
                strokeWidth = 1.dp.toPx()
            )
        }
        drawLine(
            color = NeoColors.BorderSubtle.copy(alpha = 0.35f),
            start = Offset(0f, h * 0.5f),
            end = Offset(w, h * 0.5f),
            strokeWidth = 1.dp.toPx()
        )

        // Neural links + animated flowing packet pulses
        for (i in 0 until coords.lastIndex) {
            val a = Offset(coords[i].first * w, coords[i].second * h)
            val b = Offset(coords[i + 1].first * w, coords[i + 1].second * h)
            val edgeActive = (i + 1) in completedIds || (i + 2) <= highestUnlockedId
            val edgeColor = when {
                isAlert && (i + 2) == activeModuleId -> NeoColors.CrimsonRed
                (i + 1) in completedIds -> NeoColors.MatrixGreen.copy(alpha = 0.72f)
                edgeActive -> NeoColors.CyberCyan.copy(alpha = 0.45f)
                else -> NeoColors.BorderSubtle
            }
            drawLine(color = edgeColor, start = a, end = b, strokeWidth = 2.dp.toPx())

            if (edgeActive) {
                val t = (packetPhase + i * 0.17f) % 1f
                val px = a.x + (b.x - a.x) * t
                val py = a.y + (b.y - a.y) * t
                drawCircle(
                    color = if (isAlert) NeoColors.CrimsonRed else NeoColors.MatrixGreen,
                    radius = 3.2.dp.toPx(),
                    center = Offset(px, py)
                )
            }
        }

        // Draw sector nodes
        coords.forEachIndexed { idx, pair ->
            val id = idx + 1
            val center = Offset(pair.first * w, pair.second * h)
            val isBoss = id == 7
            val isDone = id in completedIds
            val isUnlocked = id <= highestUnlockedId || isDone
            val isCurrent = id == activeModuleId
            val nodeColor = when {
                isBoss && !isDone -> NeoColors.CrimsonRed
                isDone -> NeoColors.MatrixGreen
                isUnlocked -> NeoColors.CyberCyan
                else -> NeoColors.TextMuted
            }

            if (isCurrent) {
                drawCircle(
                    color = nodeColor.copy(alpha = 0.24f),
                    radius = 15.dp.toPx() * pulseRadius,
                    center = center
                )
            }

            if (isBoss) {
                val ringRadius = 15.dp.toPx()
                drawArc(
                    color = if (isDone) NeoColors.MatrixGreen else NeoColors.CrimsonRed,
                    startAngle = sweepAngle,
                    sweepAngle = 210f,
                    useCenter = false,
                    topLeft = Offset(center.x - ringRadius, center.y - ringRadius),
                    size = Size(ringRadius * 2, ringRadius * 2),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            drawCircle(
                color = NeoColors.ObsidianVoid,
                radius = if (isBoss) 8.5.dp.toPx() else 6.5.dp.toPx(),
                center = center
            )
            drawCircle(
                color = nodeColor,
                radius = if (isBoss) 8.5.dp.toPx() else 6.5.dp.toPx(),
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
            if (isDone || isCurrent) {
                drawCircle(
                    color = nodeColor,
                    radius = 2.8.dp.toPx(),
                    center = center
                )
            }
        }
    }
}

/**
 * Subtle animated CRT scanline + phosphor sweep overlay across the Android app interface.
 */
@Composable
fun CrtScanlineOverlay(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "crtOverlay")
    val beamProgress by transition.animateFloat(
        initialValue = -0.15f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "crtBeam"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val step = 4.dp.toPx()
        var y = 0f
        while (y < size.height) {
            drawLine(
                color = Color.Black.copy(alpha = 0.14f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.2.dp.toPx()
            )
            y += step
        }

        val beamHeight = 76.dp.toPx()
        val beamTop = beamProgress * size.height
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    NeoColors.CyberCyan.copy(alpha = 0.045f),
                    NeoColors.MatrixGreen.copy(alpha = 0.025f),
                    Color.Transparent
                ),
                startY = beamTop,
                endY = beamTop + beamHeight
            ),
            topLeft = Offset(0f, beamTop),
            size = Size(size.width, beamHeight)
        )
    }
}

@Composable
fun RamInspectorPanel(
    title: String,
    emptyText: String,
    slots: List<RamVariableSlot>,
    modifier: Modifier = Modifier
) {
    CyberFramePanel(
        modifier = modifier.fillMaxWidth(),
        borderColor = NeoColors.CyberCyan.copy(alpha = 0.65f),
        fillColor = NeoColors.TerminalSurface
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = title,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = NeoColors.CyberCyan
            )
            if (slots.isEmpty()) {
                Text(
                    text = emptyText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 12.sp,
                    color = NeoColors.TextMuted
                )
            } else {
                slots.forEach { slot ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(NeoColors.ObsidianVoid)
                            .border(1.dp, NeoColors.BorderSubtle, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${slot.name} (${slot.type})",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = NeoColors.MatrixGreen
                        )
                        Text(
                            text = slot.preview,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = NeoColors.TextPrimary
                        )
                    }
                }
            }
        }
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
                    .heightIn(min = 40.dp)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
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
