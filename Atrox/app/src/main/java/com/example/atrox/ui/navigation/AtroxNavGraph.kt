package com.example.atrox.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.atrox.navigation.NavigationDestination
import com.example.atrox.ui.auth.splash.SplashScreen
import com.example.atrox.ui.auth.onboarding.OnboardingScreen1
import com.example.atrox.ui.auth.onboarding.OnboardingScreen2
import com.example.atrox.ui.auth.onboarding.OnboardingScreen3
import com.example.atrox.ui.auth.onboarding.OnboardingScreen4
import com.example.atrox.ui.auth.login.LoginScreen

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

object Onboarding3Destination : NavigationDestination {
    override val route = "onboarding3"
    override val titleRes = 0 
}

object Onboarding4Destination : NavigationDestination {
    override val route = "onboarding4"
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
                    navController.popBackStack() 
                },
                onNavigateToNext = {
                    // Navigate to Onboarding 3
                    navController.navigate(Onboarding3Destination.route)
                },
                onNavigateToSkip = {
                    // Navigate directly to Home
                    navController.navigate(HomeDestination.route) {
                        popUpTo(SplashDestination.route) { inclusive = true }
                    }
                }
            )
        }

        // --- Onboarding 3 ---
        composable(route = Onboarding3Destination.route) {
            OnboardingScreen3(
                onNavigateBack = {
                    navController.popBackStack() 
                },
                onNavigateToNext = {
                    navController.navigate(Onboarding4Destination.route)
                },
                onNavigateToSkip = {
                    navController.navigate(HomeDestination.route) {
                        popUpTo(SplashDestination.route) { inclusive = true }
                    }
                }
            )
        }

        // --- Onboarding 4 ---
        composable(route = Onboarding4Destination.route) {
            OnboardingScreen4(
                onNavigateBack = {
                    navController.popBackStack() 
                },
                onNavigateToDashboard = {
                    navController.navigate(HomeDestination.route) {
                        popUpTo(SplashDestination.route) { inclusive = true }
                    }
                },
                onNavigateToSkip = {
                    navController.navigate(HomeDestination.route) {
                        popUpTo(SplashDestination.route) { inclusive = true }
                    }
                }
            )
        }
        
        // --- Login Screen ---
        composable(route = LoginDestination.route) {
            LoginScreen(
                onNavigateToOnboarding = {
                    navController.navigate(OnboardingDestination.route) {
                        popUpTo(LoginDestination.route) { inclusive = true }
                    }
                },
                onNavigateToForgotPassword = {
                    // Handle password reset navigation
                }
            )
        }
        
        composable(route = HomeDestination.route) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Home Screen Placeholder")
            }
        }
    }
}
