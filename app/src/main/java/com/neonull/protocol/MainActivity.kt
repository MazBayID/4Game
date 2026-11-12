package com.neonull.protocol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
    ) {
        when (state.currentScreen) {
            NeoScreen.ONBOARDING -> OnboardingScreen(
                selectedLanguage = state.language,
                strings = strings,
                onSelectLanguage = viewModel::selectLanguage,
                onEnterHub = viewModel::enterHub
            )

            NeoScreen.HUB -> StoryHubScreen(
                state = state,
                strings = strings,
                onSelectLanguage = viewModel::selectLanguage,
                onOpenTheory = viewModel::openModuleTheory,
                onOpenTerminal = viewModel::openModuleTerminal
            )

            NeoScreen.THEORY -> TheoryViewerScreen(
                state = state,
                strings = strings,
                onSelectLanguage = viewModel::selectLanguage,
                onBackToHub = viewModel::enterHub,
                onProceedToTerminal = viewModel::proceedFromTheoryToTerminal
            )

            NeoScreen.TERMINAL -> TerminalLearningScreen(
                state = state,
                strings = strings,
                onSelectLanguage = viewModel::selectLanguage,
                onBackToHub = viewModel::enterHub,
                onCodeChange = viewModel::updateCode,
                onInsertSnippet = viewModel::insertSnippet,
                onRunExploit = viewModel::runExploit,
                onRequestHint = viewModel::requestHint,
                onResetCode = viewModel::resetCode,
                onNextModule = viewModel::advanceToNextModule
            )
        }
    }
}
