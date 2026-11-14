package com.neonull.protocol.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.neonull.protocol.R
import com.neonull.protocol.assets.GameAssetCatalog
import com.neonull.protocol.assets.GameAssetSprite
import com.neonull.protocol.data.ProtocolModule
import com.neonull.protocol.localization.AppLanguage
import com.neonull.protocol.localization.UiStrings
import com.neonull.protocol.syntax.SyntaxHighlightedCodeBlock
import com.neonull.protocol.ui.components.CyberFramePanel
import com.neonull.protocol.ui.theme.NeoColors

@Composable
fun CipherCodexDialog(
    isOpen: Boolean,
    language: AppLanguage,
    strings: UiStrings,
    modules: List<ProtocolModule>,
    completedIds: Set<Int>,
    onClose: () -> Unit,
    assetCatalog: GameAssetCatalog? = null
) {
    if (!isOpen) return

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 640.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(NeoColors.DeepNavyBlack)
                .border(1.2.dp, NeoColors.CyberCyan.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, NeoColors.MatrixGreen, RoundedCornerShape(8.dp))
                    ) {
                        if (assetCatalog != null) {
                            GameAssetSprite(
                                catalog = assetCatalog,
                                assetKey = "ic_codex",
                                modifier = Modifier.fillMaxSize(),
                                fallback = {
                                    Image(
                                        painter = painterResource(id = R.drawable.cipher_codex_artifact),
                                        contentDescription = strings.codexTitle,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.cipher_codex_artifact),
                                contentDescription = strings.codexTitle,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    if (assetCatalog != null) {
                        GameAssetSprite(catalog = assetCatalog, assetKey = "ic_inventory", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                    }
                    Text(
                        text = strings.codexTitle,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = NeoColors.MatrixGreen
                    )
                }
                OutlinedButton(onClick = onClose, shape = RoundedCornerShape(6.dp)) {
                    Text(
                        text = "X",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = NeoColors.CrimsonRed
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(modules, key = { it.id }) { mod ->
                    val unlocked = mod.id in completedIds
                    CyberFramePanel(
                        borderColor = if (unlocked) NeoColors.MatrixGreen else NeoColors.BorderSubtle,
                        fillColor = NeoColors.TerminalSurface
                    ) {
                        Column {
                            Text(
                                text = "${mod.actCode} · ${mod.title.resolve(language)}",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (unlocked) NeoColors.CyberCyan else NeoColors.TextMuted
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            if (unlocked) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (assetCatalog != null) {
                                        GameAssetSprite(catalog = assetCatalog, assetKey = "ic_solution", modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        GameAssetSprite(catalog = assetCatalog, assetKey = "ic_completed", modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = strings.completedLabel,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = NeoColors.MatrixGreen
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = mod.pythonConceptExplanation.resolve(language),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = NeoColors.TextPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                SyntaxHighlightedCodeBlock(
                                    code = mod.codeExample,
                                    borderColor = NeoColors.MatrixGreen.copy(alpha = 0.55f)
                                )
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (assetCatalog != null) {
                                        GameAssetSprite(
                                            catalog = assetCatalog,
                                            assetKey = "ic_locked",
                                            modifier = Modifier.size(28.dp)
                                        )
                                    } else {
                                        Image(
                                            painter = painterResource(id = R.drawable.synthex_gate_badge),
                                            contentDescription = strings.lockedLabel,
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = strings.lockedLabel,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        color = NeoColors.TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
