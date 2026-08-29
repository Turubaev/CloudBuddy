package dev.catandbunny.cloudbuddy.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.catandbunny.cloudbuddy.core.model.GameMode
import dev.catandbunny.cloudbuddy.feature.chat.ChatScreen
import dev.catandbunny.cloudbuddy.feature.checkin.CheckInScreen
import dev.catandbunny.cloudbuddy.feature.game.GameScreen
import dev.catandbunny.cloudbuddy.feature.home.HomeScreen
import dev.catandbunny.cloudbuddy.feature.memory.MemoryScreen
import dev.catandbunny.cloudbuddy.feature.onboarding.OnboardingScreen
import dev.catandbunny.cloudbuddy.feature.settings.SettingsScreen
import dev.catandbunny.cloudbuddy.feature.settings.PersonalApiScreen
import dev.catandbunny.cloudbuddy.feature.subscription.TariffsScreen
import dev.catandbunny.cloudbuddy.core.model.hostedChatAllowance
import android.app.Activity
import dev.catandbunny.cloudbuddy.ui.component.LoadingScreen

private object Route {
    const val Splash = "splash"
    const val Onboarding = "onboarding"
    const val Home = "home"
    const val CheckIn = "check_in"
    const val Chat = "chat"
    const val Settings = "settings"
    const val Memory = "memory"
    const val PersonalApi = "personal_api"
    const val Tariffs = "tariffs"
    const val Game = "game/{mode}"

    fun game(mode: GameMode) = "game/${mode.name}"
}

@Composable
fun CloudBuddyApp() {
    val context = LocalContext.current
    val container = remember { AppContainer(context) }
    val viewModel: CloudBuddyViewModel = viewModel(factory = CloudBuddyViewModel.Factory(container))
    val state by viewModel.buddyState.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val isSending by viewModel.isSending.collectAsState()
    val hasPersonalApiKey by viewModel.hasPersonalApiKey.collectAsState()
    val personalApiNotice by viewModel.personalApiNotice.collectAsState()
    val subscriptionState by viewModel.subscriptionState.collectAsState()
    val navController = rememberNavController()

    LaunchedEffect(state.isLoaded) {
        if (state.isLoaded) {
            navController.navigate(if (state.onboardingComplete) Route.Home else Route.Onboarding) {
                popUpTo(Route.Splash) { inclusive = true }
            }
        }
    }

    NavHost(navController = navController, startDestination = Route.Splash) {
        composable(Route.Splash) { LoadingScreen() }
        composable(Route.Onboarding) {
            OnboardingScreen { name, personality ->
                viewModel.completeOnboarding(name, personality)
                navController.navigate(Route.CheckIn) {
                    popUpTo(Route.Onboarding) { inclusive = true }
                }
            }
        }
        composable(Route.Home) {
            HomeScreen(
                state = state,
                onCheckIn = { navController.navigate(Route.CheckIn) },
                onChat = { navController.navigate(Route.Chat) },
                onClassicGame = { navController.navigate(Route.game(GameMode.CLASSIC)) },
                onCalmGame = { navController.navigate(Route.game(GameMode.CALM)) },
                onSettings = { navController.navigate(Route.Settings) },
            )
        }
        composable(Route.CheckIn) {
            CheckInScreen(
                buddyName = state.buddyName,
                onBack = { navController.popBackStack() },
                onSave = { mood, note ->
                    viewModel.saveCheckIn(mood, note)
                    navController.navigate(Route.Home) {
                        popUpTo(Route.Home) { inclusive = true }
                    }
                },
            )
        }
        composable(Route.Chat) {
            LaunchedEffect(Unit) { viewModel.ensureGreeting() }
            ChatScreen(
                buddyName = state.buddyName,
                weather = state.weather,
                messages = messages,
                isSending = isSending,
                personalApiEnabled = state.personalApiEnabled,
                hasPersonalApiKey = hasPersonalApiKey,
                aiModel = state.aiModel,
                subscriptionTier = state.subscriptionTier,
                allowance = state.hostedChatAllowance(),
                onTariffs = { navController.navigate(Route.Tariffs) },
                onBack = { navController.popBackStack() },
                onSend = viewModel::sendMessage,
            )
        }
        composable(
            route = Route.Game,
            arguments = listOf(navArgument("mode") { type = NavType.StringType }),
        ) { backStackEntry ->
            val mode = runCatching {
                GameMode.valueOf(backStackEntry.arguments?.getString("mode").orEmpty())
            }.getOrDefault(GameMode.CLASSIC)
            GameScreen(
                mode = mode,
                onBack = { navController.popBackStack() },
                onFinished = viewModel::recordGame,
            )
        }
        composable(Route.Settings) {
            SettingsScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onMemory = { navController.navigate(Route.Memory) },
                onPersonalApi = { navController.navigate(Route.PersonalApi) },
                onTariffs = { navController.navigate(Route.Tariffs) },
                onSoundChanged = viewModel::setSoundEnabled,
                onRemindersChanged = viewModel::setGentleReminders,
                onMemoryChanged = viewModel::setMemoryEnabled,
                onReset = {
                    viewModel.reset()
                    navController.navigate(Route.Onboarding) {
                        popUpTo(0)
                    }
                },
            )
        }
        composable(Route.Memory) {
            MemoryScreen(
                memories = state.memories,
                enabled = state.memoryEnabled,
                onBack = { navController.popBackStack() },
                onClear = viewModel::clearMemories,
            )
        }
        composable(Route.PersonalApi) {
            PersonalApiScreen(
                enabled = state.personalApiEnabled,
                hasKey = hasPersonalApiKey,
                selectedModel = state.aiModel,
                notice = personalApiNotice,
                onBack = { navController.popBackStack() },
                onEnabledChanged = viewModel::setPersonalApiEnabled,
                onModelChanged = viewModel::setAiModel,
                onSaveKey = viewModel::savePersonalApiKey,
                onDeleteKey = viewModel::deletePersonalApiKey,
            )
        }
        composable(Route.Tariffs) {
            TariffsScreen(
                state = state,
                subscription = subscriptionState,
                hasPersonalApiKey = hasPersonalApiKey,
                onBack = { navController.popBackStack() },
                onPurchasePlus = { (context as? Activity)?.let(viewModel::purchasePlus) },
                onRefresh = viewModel::refreshSubscription,
                onOpenByok = { navController.navigate(Route.PersonalApi) },
            )
        }
    }
}
