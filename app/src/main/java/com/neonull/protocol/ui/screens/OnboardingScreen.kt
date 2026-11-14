package com.neonull.protocol.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neonull.protocol.R
import com.neonull.protocol.assets.GameAssetCatalog
import com.neonull.protocol.assets.GameAssetSprite
import com.neonull.protocol.localization.AppLanguage
import com.neonull.protocol.localization.UiStrings
import com.neonull.protocol.ui.components.CyberFramePanel
import com.neonull.protocol.ui.theme.NeoColors

@Composable
fun OnboardingScreen(
    assetCatalog: GameAssetCatalog?,
    selectedLanguage: AppLanguage,
    strings: UiStrings,
    onSelectLanguage: (AppLanguage) -> Unit,
    onEnterHub: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NeoColors.DeepNavyBlack)
    ) {
        if (assetCatalog != null) {
            GameAssetSprite(
                catalog = assetCatalog,
                assetKey = "bg_hacker_base",
                modifier = Modifier.fillMaxSize().alpha(0.26f),
                fallback = {
                    Image(
                        painter = painterResource(id = R.drawable.neonull_sector_backdrop),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.neonull_sector_backdrop),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().alpha(0.26f)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            NeoColors.DeepNavyBlack.copy(alpha = 0.65f),
                            NeoColors.DeepNavyBlack.copy(alpha = 0.94f),
                            NeoColors.ObsidianVoid
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.neonull_crest_emblem),
                        contentDescription = "NEO-NULL Crest",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, NeoColors.CyberCyan, RoundedCornerShape(10.dp))
                    ) {
                        if (assetCatalog != null) {
                            GameAssetSprite(
                                catalog = assetCatalog,
                                assetKey = "glitch_portrait",
                                modifier = Modifier.fillMaxSize(),
                                fallback = {
                                    Image(
                                        painter = painterResource(id = R.drawable.glitch_mentor_avatar),
                                        contentDescription = "GLITCH",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.glitch_mentor_avatar),
                                contentDescription = "GLITCH",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = strings.appTitle,
                            style = MaterialTheme.typography.headlineLarge,
                            color = NeoColors.MatrixGreen
                        )
                        Text(
                            text = strings.appSubtitle,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = NeoColors.CrimsonRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                CyberFramePanel(
                    borderColor = NeoColors.CrimsonRed,
                    fillColor = NeoColors.TerminalSurface.copy(alpha = 0.92f)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (assetCatalog != null) GameAssetSprite(catalog = assetCatalog, assetKey = "ic_settings", modifier = Modifier.size(16.dp))
                            Text(
                                text = strings.operativeAliasLabel,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = NeoColors.CyberCyan
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = strings.selectLanguagePrompt,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                AppLanguage.entries.forEach { lang ->
                    val active = lang == selectedLanguage
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (active) NeoColors.ElevatedPanel else NeoColors.TerminalSurface
                            )
                            .border(
                                width = if (active) 1.5.dp else 1.dp,
                                color = if (active) NeoColors.MatrixGreen else NeoColors.BorderSubtle,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onSelectLanguage(lang) }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = lang.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (active) NeoColors.MatrixGreen else NeoColors.TextPrimary
                            )
                            Text(
                                text = lang.nativeSubtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 12.sp
                            )
                        }
                        Text(
                            text = lang.code,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (active) NeoColors.CyberCyan else NeoColors.TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onEnterHub,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeoColors.MatrixGreen,
                    contentColor = NeoColors.ObsidianVoid
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = strings.initiateSessionBtn,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}
