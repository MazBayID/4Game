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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
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
import com.neonull.protocol.ui.components.CyberFramePanel
import com.neonull.protocol.ui.components.LanguageSwitchRow
import com.neonull.protocol.ui.theme.NeoColors
import com.neonull.protocol.viewmodel.LearningUiState

@Composable
fun StoryHubScreen(
    state: LearningUiState,
    strings: UiStrings,
    onSelectLanguage: (AppLanguage) -> Unit,
    onOpenTheory: (Int) -> Unit,
    onOpenTerminal: (Int) -> Unit
) {
    val highestUnlocked = state.highestUnlockedModuleId()
    val progressFraction = state.completedModuleIds.size.toFloat() / state.modules.size.toFloat()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NeoColors.DeepNavyBlack)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.sectorYearLabel,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeoColors.CrimsonRed
                    )
                    Text(
                        text = strings.hubTitle,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                LanguageSwitchRow(
                    selected = state.language,
                    onSelect = onSelectLanguage
                )
            }
        }

        item {
            CyberFramePanel(
                borderColor = if (state.isCampaignComplete) NeoColors.MatrixGreen else NeoColors.CyberCyan,
                fillColor = NeoColors.TerminalSurface
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatColumn(
                            label = strings.ramStatLabel,
                            value = "${state.ramMb} MB",
                            accent = NeoColors.CyberCyan
                        )
                        StatColumn(
                            label = strings.xpStatLabel,
                            value = "${state.xp} XP",
                            accent = NeoColors.MatrixGreen
                        )
                        StatColumn(
                            label = strings.modulesProgressLabel,
                            value = "${state.completedModuleIds.size}/${state.modules.size}",
                            accent = NeoColors.TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        color = NeoColors.MatrixGreen,
                        trackColor = NeoColors.BorderSubtle,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${strings.rankLabel}: ${state.rankTitle(state.language)}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = NeoColors.TextSecondary
                    )
                }
            }
        }

        if (state.isCampaignComplete) {
            item {
                CyberFramePanel(
                    borderColor = NeoColors.MatrixGreen,
                    fillColor = NeoColors.ElevatedPanel
                ) {
                    Column {
                        Text(
                            text = strings.victoryTitle,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = NeoColors.MatrixGreen
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = strings.victorySubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = NeoColors.TextPrimary
                        )
                    }
                }
            }
        }

        items(state.modules, key = { it.id }) { module ->
            val isCompleted = module.id in state.completedModuleIds
            val isUnlocked = module.id <= highestUnlocked || isCompleted
            val borderColor = when {
                module.isBossStage && isUnlocked -> NeoColors.CrimsonRed
                isCompleted -> NeoColors.MatrixGreen
                isUnlocked -> NeoColors.CyberCyan
                else -> NeoColors.BorderSubtle
            }
            val statusText = when {
                isCompleted -> strings.completedLabel
                module.isBossStage && isUnlocked -> strings.bossAlertLabel
                isUnlocked -> strings.activeLabel
                else -> strings.lockedLabel
            }

            CyberFramePanel(
                borderColor = borderColor,
                fillColor = if (module.isBossStage && isUnlocked) NeoColors.CrimsonDarkTint else NeoColors.TerminalSurface
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${module.actCode} · M0${module.id} · $statusText",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = borderColor
                        )
                        Text(
                            text = "+${module.xpReward} XP / ${module.ramRewardMb} MB",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = NeoColors.TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (module.isBossStage) {
                            Image(
                                painter = painterResource(id = R.drawable.overlord_boss_core),
                                contentDescription = "AI OVERLORD",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, NeoColors.CrimsonRed, RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = module.missionName.resolve(state.language),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "${module.title.resolve(state.language)} · ${module.conceptTag.resolve(state.language)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 12.sp
                            )
                        }
                    }

                    if (isUnlocked) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onOpenTheory(module.id) },
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = strings.openTheoryBtn,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = NeoColors.CyberCyan
                                )
                            }
                            Button(
                                onClick = { onOpenTerminal(module.id) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (module.isBossStage) NeoColors.CrimsonRed else NeoColors.MatrixGreen,
                                    contentColor = if (module.isBossStage) NeoColors.TextPrimary else NeoColors.ObsidianVoid
                                ),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = strings.launchTerminalBtn,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
private fun StatColumn(
    label: String,
    value: String,
    accent: androidx.compose.ui.graphics.Color
) {
    Column {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = NeoColors.TextMuted
        )
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = accent
        )
    }
}
