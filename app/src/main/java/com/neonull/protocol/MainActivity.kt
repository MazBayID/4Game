package com.neonull.protocol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.neonull.protocol.audio.CyberAudioEngine
import com.neonull.protocol.ui.components.CrtScanlineOverlay
import com.neonull.protocol.ui.screens.CipherCodexDialog
import com.neonull.protocol.ui.screens.OnboardingScreen
import com.neonull.protocol.ui.screens.StoryHubScreen
import com.neonull.protocol.ui.screens.TerminalLearningScreen
import com.neonull.protocol.ui.screens.TheoryViewerScreen
import com.neonull.protocol.ui.theme.NeoColors
import com.neonull.protocol.ui.theme.NeoNullTheme
import com.neonull.protocol.viewmodel.LearningViewModel
import com.neonull.protocol.viewmodel.NeoScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NeoNullTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = NeoColors.DeepNavyBlack
                ) {
                    NeoNullRootApp()
                }
            }
        }
    }
}

@Composable
fun NeoNullRootApp(
    viewModel: LearningViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val strings = viewModel.getStrings(state.language)
    val audioEngine = remember { CyberAudioEngine() }

    LaunchedEffect(state.audioEnabled) {
        audioEngine.enabled = state.audioEnabled
    }

    LaunchedEffect(state.evaluationResult) {
        val res = state.evaluationResult ?: return@LaunchedEffect
        if (res.isSuccess) {
            audioEngine.playBreachSuccess()
        } else {
            audioEngine.playCrimsonAlert()
        }
    }

    BackHandler(
        enabled = state.codexOpen ||
            state.currentScreen == NeoScreen.TERMINAL ||
            state.currentScreen == NeoScreen.THEORY
    ) {
        audioEngine.playKeyTick()
        if (state.codexOpen) {
            viewModel.closeCodex()
        } else {
            viewModel.enterHub()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
    ) {
        when (state.currentScreen) {
            NeoScreen.ONBOARDING -> OnboardingScreen(
                selectedLanguage = state.language,
                strings = strings,
                onSelectLanguage = { lang ->
                    audioEngine.playKeyTick()
                    viewModel.selectLanguage(lang)
                },
                onEnterHub = {
                    audioEngine.playKeyTick()
                    viewModel.enterHub()
                }
            )

            NeoScreen.HUB -> StoryHubScreen(
                state = state,
                strings = strings,
                onSelectLanguage = { lang ->
                    audioEngine.playKeyTick()
                    viewModel.selectLanguage(lang)
                },
                onToggleAudio = {
                    viewModel.toggleAudio()
                    audioEngine.playKeyTick()
                },
                onOpenCodex = {
                    audioEngine.playKeyTick()
                    viewModel.openCodex()
                },
                onOpenSandbox = {
                    audioEngine.playKeyTick()
                    viewModel.openSandbox()
                },
                onOpenTheory = { id ->
                    audioEngine.playKeyTick()
                    viewModel.openModuleTheory(id)
                },
                onOpenTerminal = { id ->
                    audioEngine.playKeyTick()
                    viewModel.openModuleTerminal(id)
                }
            )

            NeoScreen.THEORY -> TheoryViewerScreen(
                state = state,
                strings = strings,
                onSelectLanguage = { lang ->
                    audioEngine.playKeyTick()
                    viewModel.selectLanguage(lang)
                },
                onBackToHub = {
                    audioEngine.playKeyTick()
                    viewModel.enterHub()
                },
                onProceedToTerminal = {
                    audioEngine.playKeyTick()
                    viewModel.proceedFromTheoryToTerminal()
                }
            )

            NeoScreen.TERMINAL -> TerminalLearningScreen(
                state = state,
                strings = strings,
                onSelectLanguage = { lang ->
                    audioEngine.playKeyTick()
                    viewModel.selectLanguage(lang)
                },
                onBackToHub = {
                    audioEngine.playKeyTick()
                    viewModel.enterHub()
                },
                onOpenCodex = {
                    audioEngine.playKeyTick()
                    viewModel.openCodex()
                },
                onSelectRadarNode = { id ->
                    audioEngine.playKeyTick()
                    viewModel.openModuleTerminal(id)
                },
                onCodeChange = viewModel::updateCode,
                onInsertSnippet = { snippet ->
                    audioEngine.playKeyTick()
                    viewModel.insertSnippet(snippet)
                },
                onRunExploit = {
                    audioEngine.playDeployBeam()
                    viewModel.runExploit()
                },
                onRequestHint = {
                    audioEngine.playKeyTick()
                    viewModel.requestHint()
                },
                onResetCode = {
                    audioEngine.playKeyTick()
                    viewModel.resetCode()
                },
                onNextModule = {
                    audioEngine.playKeyTick()
                    viewModel.advanceToNextModule()
                }
            )
        }

        CipherCodexDialog(
            isOpen = state.codexOpen,
            language = state.language,
            strings = strings,
            modules = state.modules,
            completedIds = state.completedModuleIds,
            onClose = {
                audioEngine.playKeyTick()
                viewModel.closeCodex()
            }
        )

        CrtScanlineOverlay()
    }
}
