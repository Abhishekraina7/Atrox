package com.example.atrox.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material.icons.rounded.GpsFixed
import androidx.compose.material.icons.rounded.Assignment
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.atrox.ui.home.tasks.TaskScreen
import com.example.atrox.ui.main.dashboard.DashboardScreen

private val ColorBackground = Color(0xFF0A0A0F)
private val ColorSurface = Color(0xFF14141E)
private val ColorAccent = Color(0xFF6C63FF)
private val ColorTextSecondary = Color(0xFF8888A0)

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Dashboard : BottomNavItem("dashboard", Icons.Rounded.Home, "HOME")
    object Tasks : BottomNavItem("tasks", Icons.Rounded.TaskAlt, "TASKS")
    object Focus : BottomNavItem("focus", Icons.Rounded.GpsFixed, "FOCUS")
    object Notes : BottomNavItem("notes", Icons.Rounded.Assignment, "NOTES")
    object Profile : BottomNavItem("profile", Icons.Rounded.Person, "PROFILE")
}

@Composable
fun MainScreen(
    rootNavController: NavHostController
) {
    val bottomNavController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            BottomNavBar(navController = bottomNavController)
        },
        containerColor = ColorBackground
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = BottomNavItem.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Dashboard.route) {
                DashboardScreen()
            }
            composable(BottomNavItem.Tasks.route) {
                TaskScreen()
            }
            composable(BottomNavItem.Focus.route) { PlaceholderScreen("Focus") }
            composable(BottomNavItem.Notes.route) { PlaceholderScreen("Notes") }
            composable(BottomNavItem.Profile.route) { PlaceholderScreen("Profile") }
        }
    }
}

@Composable
fun BottomNavBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Tasks,
        BottomNavItem.Focus,
        BottomNavItem.Notes,
        BottomNavItem.Profile
    )
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    NavigationBar(
        containerColor = ColorSurface,
        contentColor = ColorTextSecondary
    ) {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label = { Text(text = item.label, fontSize = 10.sp) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) { saveState = true }
                        }
                        launchSingleTop = true //there will be only one screen in backstack to avoid infinite back loops
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ColorAccent,
                    selectedTextColor = ColorAccent,
                    unselectedIconColor = ColorTextSecondary,
                    unselectedTextColor = ColorTextSecondary,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize().background(ColorBackground), contentAlignment = Alignment.Center) {
        Text(title, color = Color.White)
    }
}
