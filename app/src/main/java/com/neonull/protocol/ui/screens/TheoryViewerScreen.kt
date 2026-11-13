package com.neonull.protocol.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neonull.protocol.R
import com.neonull.protocol.localization.AppLanguage
import com.neonull.protocol.localization.UiStrings
import com.neonull.protocol.syntax.SyntaxHighlightedCodeBlock
import com.neonull.protocol.ui.components.CyberFramePanel
import com.neonull.protocol.ui.components.LanguageSwitchRow
import com.neonull.protocol.ui.theme.NeoColors
import com.neonull.protocol.viewmodel.LearningUiState

@Composable
fun TheoryViewerScreen(
    state: LearningUiState,
    strings: UiStrings,
    onSelectLanguage: (AppLanguage) -> Unit,
    onBackToHub: () -> Unit,
    onProceedToTerminal: () -> Unit
) {
    val module = state.activeModule
    val accent = if (module.isBossStage) NeoColors.CrimsonRed else NeoColors.CyberCyan

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NeoColors.DeepNavyBlack)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
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
            LanguageSwitchRow(
                selected = state.language,
                onSelect = onSelectLanguage
            )
        }

        Column {
            Text(
                text = "${module.actCode} · ${strings.theoryBriefingTitle}",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = accent
            )
            Text(
                text = "${module.missionName.resolve(state.language)} — ${module.title.resolve(state.language)}",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        CyberFramePanel(
            borderColor = if (module.isBossStage) NeoColors.CrimsonRed else NeoColors.BorderSubtle,
            fillColor = if (module.isBossStage) NeoColors.CrimsonDarkTint else NeoColors.TerminalSurface
        ) {
            Column {
                Text(
                    text = strings.loreTransmissionTitle,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = accent
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = module.storyBriefing.resolve(state.language),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        module.overlordTaunt?.let { taunt ->
            CyberFramePanel(
                borderColor = NeoColors.CrimsonRed,
                fillColor = NeoColors.CrimsonDarkTint
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.synthex_gate_badge),
                        contentDescription = strings.overlordTauntTitle,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.overlordTauntTitle,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = NeoColors.CrimsonRed
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = taunt.resolve(state.language),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = NeoColors.TextPrimary
                        )
                    }
                }
            }
        }

        CyberFramePanel(
            borderColor = NeoColors.MatrixGreen,
            fillColor = NeoColors.TerminalSurface
        ) {
            Column {
                Text(
                    text = strings.pythonConceptTitle,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = NeoColors.MatrixGreen
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = module.pythonConceptExplanation.resolve(state.language),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Column {
            Text(
                text = strings.codeExampleTitle,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = NeoColors.CyberCyan
            )
            Spacer(modifier = Modifier.height(6.dp))
            SyntaxHighlightedCodeBlock(
                code = module.codeExample,
                borderColor = accent.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = onProceedToTerminal,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (module.isBossStage) NeoColors.CrimsonRed else NeoColors.MatrixGreen,
                contentColor = if (module.isBossStage) NeoColors.TextPrimary else NeoColors.ObsidianVoid
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = strings.proceedToTerminalBtn,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}
