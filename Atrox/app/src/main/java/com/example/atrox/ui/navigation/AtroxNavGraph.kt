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
import com.example.atrox.ui.auth.onboarding.OnboardingScreen1
import com.example.atrox.ui.auth.onboarding.OnboardingScreen2

// ------------------------------------
// Navigation Destinations
// ------------------------------------
object SplashDestination : NavigationDestination {
    override val route = "splash"
    override val titleRes = 0 
}

object OnboardingDestination : NavigationDestination {
    override val route = "onboarding"
    override val titleRes = 0 
}

object Onboarding2Destination : NavigationDestination {
    override val route = "onboarding2"
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
        
        // --- Onboarding 1 ---
        composable(route = OnboardingDestination.route) {
            OnboardingScreen1(
                onNavigateToNext = {
                    // Navigate to Onboarding 2
                    navController.navigate(Onboarding2Destination.route)
                },
                onNavigateToLogin = {
                    navController.navigate(LoginDestination.route) {
                        popUpTo(OnboardingDestination.route) { inclusive = true }
                    }
                }
            )
        }
        
        // --- Onboarding 2 ---
        composable(route = Onboarding2Destination.route) {
            OnboardingScreen2(
                onNavigateBack = {
                    navController.popBackStack() // Goes back to Onboarding 1
                },
                onNavigateToNext = {
                    // Navigate to future Onboarding 3 or Login/Home
                    navController.navigate(LoginDestination.route) {
                        // Pop up to Splash or remove everything from backstack
                        popUpTo(SplashDestination.route) { inclusive = true }
                    }
                },
                onNavigateToSkip = {
                    // Navigate directly to Home
                    navController.navigate(HomeDestination.route) {
                        popUpTo(SplashDestination.route) { inclusive = true }
                    }
                }
            )
        }
        
        // --- Placeholders ---
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
