package com.example.a216553_praavieenrajj_mrnelson_project_2.Navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.a216553_praavieenrajj_mrnelson_project_2.Screens.*
import com.example.a216553_praavieenrajj_mrnelson_project_2.ViewModel.UserViewModel

enum class Screen {
    LOGIN, REGISTER, HOME, PROFILE, ADD_ENVELOPE, COMMUNITY_HUB, INSIGHTS
}

@Composable
fun AppNavigator(userViewModel: UserViewModel = viewModel()) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.LOGIN.name) {
        composable(Screen.LOGIN.name) {
            LoginScreen(
                userViewModel = userViewModel,
                onLoginSuccess = { navController.navigate(Screen.HOME.name) },
                onGoToRegister = { navController.navigate(Screen.REGISTER.name) }
            )
        }

        composable(Screen.REGISTER.name) {
            RegisterScreen(
                userViewModel = userViewModel,
                onRegisterSuccess = { navController.navigate(Screen.HOME.name) },
                onGoToLogin = { navController.popBackStack() }
            )
        }

        composable(Screen.HOME.name) {
            val profile by userViewModel.uiState.collectAsState()
            val envelopes by userViewModel.envelopes.collectAsState()
            HomeScreen(
                profile = profile,
                envelopeList = envelopes,
                userViewModel = userViewModel, // INTEGRATION: Shared data access
                onNavigateToAdd = { navController.navigate(Screen.ADD_ENVELOPE.name) },
                onViewProfile = { navController.navigate(Screen.PROFILE.name) },
                onNavigateToCommunity = { navController.navigate(Screen.COMMUNITY_HUB.name) },
                onNavigateToInsights = { navController.navigate(Screen.INSIGHTS.name) },
                onDeleteEnvelope = { userViewModel.deleteEnvelope(it) },
                onSaveSavings = { env, amt -> userViewModel.updateEnvelopeSavings(env, amt) },
                onLogout = {
                    userViewModel.clear()
                    navController.navigate(Screen.LOGIN.name) { popUpTo(0) }
                }
            )
        }

        composable(Screen.ADD_ENVELOPE.name) {
            AddEnvelopeScreen(
                onEnvelopeAdded = { t, b ->
                    userViewModel.addEnvelope(t, b)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.PROFILE.name) {
            val profile by userViewModel.uiState.collectAsState()
            ProfileScreen(profile = profile, onBack = { navController.popBackStack() })
        }

        composable(Screen.COMMUNITY_HUB.name) {
            CommunityHubScreen(
                userViewModel = userViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.INSIGHTS.name) {
            InsightsScreen(onBack = { navController.popBackStack() })
        }
    }
}
