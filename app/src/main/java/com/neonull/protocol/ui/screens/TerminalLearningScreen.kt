package com.neonull.protocol.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neonull.protocol.localization.AppLanguage
import com.neonull.protocol.localization.UiStrings
import com.neonull.protocol.syntax.SyntaxHighlightedCodeEditor
import com.neonull.protocol.ui.components.CyberFramePanel
import com.neonull.protocol.ui.components.GlitchMentorPanel
import com.neonull.protocol.ui.components.LanguageSwitchRow
import com.neonull.protocol.ui.theme.CodeTextStyle
import com.neonull.protocol.ui.theme.NeoColors
import com.neonull.protocol.viewmodel.LearningUiState

@Composable
fun TerminalLearningScreen(
    state: LearningUiState,
    strings: UiStrings,
    onSelectLanguage: (AppLanguage) -> Unit,
    onBackToHub: () -> Unit,
    onCodeChange: (String) -> Unit,
    onInsertSnippet: (String) -> Unit,
    onRunExploit: () -> Unit,
    onRequestHint: () -> Unit,
    onResetCode: () -> Unit,
    onNextModule: () -> Unit
) {
    val module = state.activeModule
    val eval = state.evaluationResult
    val errorLines = eval?.errorLines ?: emptySet()
    val hasNextModule = module.id < state.modules.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NeoColors.DeepNavyBlack)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top HUD: Back, Stats, Language Switcher
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(onClick = onBackToHub, shape = RoundedCornerShape(6.dp)) {
                Text(
                    text = "< ${strings.backToHubBtn}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = NeoColors.TextPrimary
                )
            }
            Text(
                text = "RAM ${state.ramMb}MB · ${state.xp}XP",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = NeoColors.MatrixGreen
            )
            LanguageSwitchRow(
                selected = state.language,
                onSelect = onSelectLanguage
            )
        }

        // GLITCH AI Mentor Panel
        GlitchMentorPanel(
            title = strings.glitchMentorTitle,
            dialogue = state.glitchDialogue,
            isWarning = state.isWarningState,
            isBossStage = module.isBossStage,
            warningBannerText = strings.warningBannerText
        )

        // Mission Objective Card
        CyberFramePanel(
            borderColor = if (module.isBossStage) NeoColors.CrimsonRed else NeoColors.BorderSubtle,
            fillColor = NeoColors.TerminalSurface
        ) {
            Column {
                Text(
                    text = "${module.actCode} · ${strings.missionObjectiveTitle}: ${module.missionName.resolve(state.language)}",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (module.isBossStage) NeoColors.CrimsonRed else NeoColors.CyberCyan
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = module.objective.resolve(state.language),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        // Interactive Code Editor with Real-Time Syntax Highlighting
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.editorHeaderTitle,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = NeoColors.CyberCyan
                )
                Text(
                    text = strings.resetCodeBtn,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = NeoColors.TextSecondary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onResetCode() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            SyntaxHighlightedCodeEditor(
                code = state.codeBuffer,
                onCodeChange = onCodeChange,
                errorLines = errorLines,
                accentBorderColor = if (module.isBossStage) NeoColors.CrimsonRed else NeoColors.CyberCyan
            )
        }

        // Mobile-friendly Python snippet helpers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            module.snippetChips.forEach { snippet ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(NeoColors.ElevatedPanel)
                        .border(1.dp, NeoColors.BorderSubtle, RoundedCornerShape(6.dp))
                        .clickable { onInsertSnippet(snippet) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
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

        // Primary Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onRequestHint,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text(
                    text = strings.requestHintBtn,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = NeoColors.CyberCyan
                )
            }
            Button(
                onClick = onRunExploit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (module.isBossStage) NeoColors.CrimsonRed else NeoColors.MatrixGreen,
                    contentColor = if (module.isBossStage) NeoColors.TextPrimary else NeoColors.ObsidianVoid
                ),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text(
                    text = strings.runExploitBtn,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        // Execution Console Output
        val consoleBorder = when {
            eval == null -> NeoColors.BorderSubtle
            eval.isSuccess -> NeoColors.MatrixGreen
            else -> NeoColors.CrimsonRed
        }
        CyberFramePanel(
            borderColor = consoleBorder,
            fillColor = NeoColors.ObsidianVoid
        ) {
            Column {
                Text(
                    text = if (eval?.isSuccess == true) {
                        "${strings.consoleOutputTitle} · ${strings.missionPassedStatus}"
                    } else {
                        strings.consoleOutputTitle
                    },
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = consoleBorder
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = eval?.outputText ?: strings.noOutputPlaceholder,
                    style = CodeTextStyle.copy(
                        color = when {
                            eval == null -> NeoColors.TextMuted
                            eval.isSuccess -> NeoColors.MatrixGreen
                            else -> NeoColors.CrimsonRed
                        }
                    )
                )
            }
        }

        if (eval?.isSuccess == true) {
            Button(
                onClick = if (hasNextModule) onNextModule else onBackToHub,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeoColors.CyberCyan,
                    contentColor = NeoColors.ObsidianVoid
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = if (hasNextModule) strings.nextModuleBtn else strings.backToHubBtn,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
