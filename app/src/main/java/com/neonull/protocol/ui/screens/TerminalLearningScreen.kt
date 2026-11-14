package com.neonull.protocol.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neonull.protocol.R
import com.neonull.protocol.assets.GameAssetCatalog
import com.neonull.protocol.assets.GameAssetSprite
import com.neonull.protocol.localization.AppLanguage
import com.neonull.protocol.localization.UiStrings
import com.neonull.protocol.syntax.SyntaxHighlightedCodeEditor
import com.neonull.protocol.ui.components.CyberFramePanel
import com.neonull.protocol.ui.components.GlitchMentorPanel
import com.neonull.protocol.ui.components.LanguageSwitchRow
import com.neonull.protocol.ui.components.RamInspectorPanel
import com.neonull.protocol.ui.components.TacticalNetworkRadar
import com.neonull.protocol.ui.theme.ConsoleTextStyle
import com.neonull.protocol.ui.theme.NeoColors
import com.neonull.protocol.viewmodel.LearningUiState

/**
 * EXAPUNKS / Hacknet-inspired 3-Panel Split-Screen Terminal Deck:
 * 1. Top Panel: Status HUD (RAM, XP, Firewall Health) + GLITCH AI Mentor Briefing & Objective
 * 2. Center Panel: Flexible-height Code Editor with line-number gutter & live Python highlighting
 * 3. Bottom Panel: Context-colored [ RUN EXPLOIT ] control + Docked Output / RAM Console
 */
@Composable
fun TerminalLearningScreen(
    assetCatalog: GameAssetCatalog?,
    state: LearningUiState,
    strings: UiStrings,
    onSelectLanguage: (AppLanguage) -> Unit,
    onBackToHub: () -> Unit,
    onOpenCodex: () -> Unit,
    onSelectRadarNode: (Int) -> Unit,
    onCodeChange: (String) -> Unit,
    onInsertSnippet: (String) -> Unit,
    onRunExploit: () -> Unit,
    onRequestHint: () -> Unit,
    onResetCode: () -> Unit,
    onNextModule: () -> Unit
) {
    val module = state.activeModule
    val isSandbox = state.isSandboxMode
    val eval = state.evaluationResult
    val errorLines = eval?.errorLines ?: emptySet()
    val hasNextModule = !isSandbox && module.id < state.modules.size
    val snippets = if (isSandbox) {
        listOf("print()", "for i in range(5):", "def hack():", "return ")
    } else {
        module.snippetChips
    }

    // Bottom console tab: 0 = Execution Console, 1 = Live RAM Inspector
    var bottomTabIndex by remember { mutableIntStateOf(0) }
    var activeExecutionEffect by remember { mutableStateOf<String?>(null) }
    var previousEvaluation by remember { mutableStateOf(eval) }

    LaunchedEffect(eval) {
        if (eval !== previousEvaluation) {
            activeExecutionEffect = when {
                eval?.isSuccess == true -> "fx_breach"
                eval != null -> "fx_error"
                else -> null
            }
            previousEvaluation = eval
        }
    }
    // Optional radar drawer toggle inside Top Panel
    var showRadarInTop by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NeoColors.DeepNavyBlack)
    ) {
        if (assetCatalog != null) {
            GameAssetSprite(
                catalog = assetCatalog,
                assetKey = if (!isSandbox && module.isBossStage) "bg_overlord_domain" else "bg_cyberspace",
                modifier = Modifier.fillMaxSize().alpha(0.14f)
            )
        }
        // Subtle EXAPUNKS background matrix grid
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 28.dp.toPx()
            var x = step
            while (x < size.width) {
                drawLine(
                    color = NeoColors.DeckGridLine.copy(alpha = 0.20f),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 0.8.dp.toPx()
                )
                x += step
            }
            var y = step
            while (y < size.height) {
                drawLine(
                    color = NeoColors.DeckGridLine.copy(alpha = 0.28f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 0.8.dp.toPx()
                )
                y += step
            }
        }

        if (assetCatalog != null && activeExecutionEffect != null) {
            GameAssetSprite(
                catalog = assetCatalog,
                assetKey = activeExecutionEffect!!,
                modifier = Modifier.fillMaxSize().alpha(0.22f),
                onAnimationFinished = { activeExecutionEffect = null }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // =========================================================================
            // PANEL 1 (TOP): STATUS HUD & GLITCH AI MENTOR BRIEFING
            // =========================================================================
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(5.dp))
                        .background(NeoColors.TerminalSurface)
                        .border(1.dp, NeoColors.BorderSubtle, RoundedCornerShape(5.dp))
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "< ${strings.backToHubBtn}",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = NeoColors.CyberCyan,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onBackToHub() }
                                .padding(horizontal = 6.dp, vertical = 10.dp)
                        )
                        LanguageSwitchRow(
                            selected = state.language,
                            onSelect = onSelectLanguage
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${strings.ramStatLabel}: ${state.ramMb}M",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = NeoColors.CyberCyan,
                            maxLines = 1
                        )
                        Text(
                            text = "${strings.xpStatLabel}: ${state.xp}",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = NeoColors.MatrixGreen,
                            maxLines = 1
                        )
                        Text(
                            text = "${strings.firewallHealthLabel}: ${state.firewallHealth}%",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (state.firewallHealth <= 40) NeoColors.CrimsonRed else NeoColors.TextPrimary,
                            maxLines = 1
                        )
                    }
                }

                GlitchMentorPanel(
                    title = strings.glitchMentorTitle,
                    dialogue = state.glitchDialogue,
                    isWarning = state.isWarningState,
                    isBossStage = !isSandbox && module.isBossStage,
                    isSuccess = eval?.isSuccess == true,
                    warningBannerText = strings.warningBannerText,
                    assetCatalog = assetCatalog
                )

                // Compact Mission Objective bar + optional Tactical Grid toggle
                CyberFramePanel(
                    borderColor = when {
                        isSandbox -> NeoColors.CyberCyan
                        module.isBossStage -> NeoColors.CrimsonRed
                        else -> NeoColors.BorderSubtle
                    },
                    fillColor = NeoColors.TerminalSurface,
                    contentPadding = 10.dp
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isSandbox) {
                                    strings.sandboxTitle
                                } else {
                                    "${module.actCode} · ${module.missionName.resolve(state.language)}"
                                },
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (!isSandbox && module.isBossStage) NeoColors.CrimsonRed else NeoColors.MatrixGreen,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = if (showRadarInTop) "[ - GRID ]" else "[ + GRID ]",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp,
                                color = NeoColors.CyberCyan,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { showRadarInTop = !showRadarInTop }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                        Text(
                            text = if (isSandbox) strings.sandboxSubtitle else module.objective.resolve(state.language),
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 12.5.sp,
                            color = NeoColors.TextPrimary
                        )
                    }
                }

                if (showRadarInTop) {
                    TacticalNetworkRadar(
                        completedIds = state.completedModuleIds,
                        activeModuleId = if (isSandbox) 0 else module.id,
                        highestUnlockedId = state.highestUnlockedModuleId(),
                        isAlert = state.isWarningState,
                        onNodeClick = onSelectRadarNode
                    )
                }
            }

            // =========================================================================
            // PANEL 2 (CENTER): INTERACTIVE CODE EDITOR / TERMINAL DECK
            // =========================================================================
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.editorHeaderTitle,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        color = NeoColors.CyberCyan
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onOpenCodex() }
                                .padding(horizontal = 8.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (assetCatalog != null) {
                                GameAssetSprite(
                                    catalog = assetCatalog,
                                    assetKey = "ic_codex",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = strings.codexBtn,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = NeoColors.MatrixGreen
                            )
                        }
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onResetCode() }
                                .padding(horizontal = 8.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (assetCatalog != null) {
                                GameAssetSprite(
                                    catalog = assetCatalog,
                                    assetKey = "ic_reset",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = strings.resetCodeBtn,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = NeoColors.TextSecondary
                            )
                        }
                    }
                }

                SyntaxHighlightedCodeEditor(
                    code = state.codeBuffer,
                    onCodeChange = onCodeChange,
                    errorLines = errorLines,
                    accentBorderColor = if (!isSandbox && module.isBossStage) NeoColors.CrimsonRed else NeoColors.CyberCyan,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    snippets.forEach { snippet ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NeoColors.ElevatedPanel)
                                .border(1.dp, NeoColors.BorderSubtle, RoundedCornerShape(6.dp))
                                .clickable { onInsertSnippet(snippet) }
                                .heightIn(min = 40.dp)
                                .padding(horizontal = 9.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "+ $snippet",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = NeoColors.CyberCyan
                            )
                        }
                    }
                }
            }

            // =========================================================================
            // PANEL 3 (BOTTOM): EXECUTION CONTROL & CONSOLE / RAM OUTPUT DECK
            // =========================================================================
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!isSandbox) {
                        OutlinedButton(
                            onClick = onRequestHint,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .weight(0.42f)
                                .height(46.dp)
                        ) {
                            if (assetCatalog != null) {
                                GameAssetSprite(
                                    catalog = assetCatalog,
                                    assetKey = "ic_hint",
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                            }
                            Text(
                                text = strings.requestHintBtn,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = NeoColors.CyberCyan,
                                maxLines = 1
                            )
                        }
                    }

                    // Primary execution action; crimson remains reserved for boss stages and errors.
                    Button(
                        onClick = onRunExploit,
                        enabled = !state.isExecuting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isSandbox && module.isBossStage) NeoColors.CrimsonRed else NeoColors.MatrixGreen,
                            contentColor = if (!isSandbox && module.isBossStage) NeoColors.TextPrimary else NeoColors.ObsidianVoid
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .weight(if (isSandbox) 1f else 0.58f)
                            .height(46.dp)
                    ) {
                        if (assetCatalog != null) {
                            GameAssetSprite(
                                catalog = assetCatalog,
                                assetKey = "ic_run",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = strings.runExploitBtn,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            letterSpacing = 0.8.sp,
                            maxLines = 1
                        )
                    }
                }

                val consoleBorder = when {
                    eval == null -> NeoColors.BorderSubtle
                    eval.isSuccess -> NeoColors.MatrixGreen
                    else -> NeoColors.CrimsonRed
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 104.dp, max = 148.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(NeoColors.ObsidianVoid)
                        .border(1.2.dp, consoleBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            DeckTabPill(
                                label = if (eval?.isSuccess == true && !isSandbox) {
                                    "${strings.consoleTabLabel} · OK"
                                } else {
                                    strings.consoleTabLabel
                                },
                                selected = bottomTabIndex == 0,
                                accent = consoleBorder,
                                onClick = { bottomTabIndex = 0 }
                            )
                            DeckTabPill(
                                label = "${strings.ramTabLabel} (${eval?.ramSlots?.size ?: 0})",
                                selected = bottomTabIndex == 1,
                                accent = NeoColors.CyberCyan,
                                onClick = { bottomTabIndex = 1 }
                            )
                        }

                        if (!isSandbox && eval?.isSuccess == true) {
                            Text(
                                text = if (hasNextModule) "${strings.nextModuleBtn} >" else strings.backToHubBtn,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = NeoColors.ObsidianVoid,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(NeoColors.MatrixGreen)
                                    .clickable {
                                        if (hasNextModule) onNextModule() else onBackToHub()
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (bottomTabIndex == 0) {
                            Text(
                                text = eval?.outputText ?: strings.noOutputPlaceholder,
                                style = ConsoleTextStyle.copy(
                                    fontFamily = FontFamily(Font(R.font.vt323_regular)),
                                    fontSize = 16.sp,
                                    lineHeight = 19.sp,
                                    color = when {
                                        eval == null -> NeoColors.TextMuted
                                        eval.isSuccess -> NeoColors.MatrixGreen
                                        else -> NeoColors.CrimsonRed
                                    }
                                )
                            )
                        } else {
                            RamInspectorPanel(
                                title = strings.ramInspectorTitle,
                                emptyText = strings.ramEmptyText,
                                slots = eval?.ramSlots ?: emptyList()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeckTabPill(
    label: String,
    selected: Boolean,
    accent: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (selected) accent.copy(alpha = 0.16f) else NeoColors.TerminalSurface)
            .border(
                width = 1.dp,
                color = if (selected) accent else NeoColors.BorderSubtle,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable { onClick() }
            .heightIn(min = 36.dp)
            .padding(horizontal = 8.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 10.5.sp,
            color = if (selected) accent else NeoColors.TextSecondary
        )
    }
}
