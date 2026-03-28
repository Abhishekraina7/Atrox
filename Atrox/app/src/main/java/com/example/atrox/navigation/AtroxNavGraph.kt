package com.example.atrox.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.atrox.ui.auth.splash.SplashScreen

// ------------------------------------
// Navigation Destinations
// ------------------------------------
object SplashDestination : NavigationDestination {
    override val route = "splash"
    override val titleRes = 0 // Represents an R.string.splash_title in the future
}

object OnboardingDestination : NavigationDestination {
    override val route = "onboarding"
    override val titleRes = 0 
}

object LoginDestination : NavigationDestination {
    override val route = "login"
    override val titleRes = 0 
}

object HomeDestination : NavigationDestination {
    override val route = "home"
    override val titleRes = 0 
}

// ------------------------------------
// Main Navigation Graph
// ------------------------------------
@Composable
fun AtroxNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = SplashDestination.route, // Sets the Splash Screen as Entry point
        modifier = modifier
    ) {
        
        // --- Splash Screen ---
        composable(route = SplashDestination.route) {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate(OnboardingDestination.route) {
                        popUpTo(SplashDestination.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(LoginDestination.route) {
                        popUpTo(SplashDestination.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(HomeDestination.route) {
                        popUpTo(SplashDestination.route) { inclusive = true }
                    }
                }
            )
        }

        // --- Placeholders ---
        composable(route = OnboardingDestination.route) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Onboarding Screen Placeholder")
            }
        }
        
        composable(route = LoginDestination.route) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Login Screen Placeholder")
            }
        }
        
        composable(route = HomeDestination.route) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Home Screen Placeholder")
            }
        }
    }
}
