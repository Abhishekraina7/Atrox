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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.atrox.ui.home.focus.FocusScreen
import com.example.atrox.ui.home.tasks.TaskScreen
import com.example.atrox.ui.home.dashboard.DashboardScreen
import com.example.atrox.ui.home.notes.AddNotesScreen
import com.example.atrox.ui.home.notes.NotesScreen
import com.example.atrox.ui.home.profile.ProfileScreen
import com.example.atrox.ui.home.profile.RegulatorScreen
import com.example.atrox.ui.home.profile.StreakHistoryScreen

// Focus session route with a required taskId argument
const val FOCUS_ROUTE = "focus_session/{taskId}"
fun focusRoute(taskId: String) = "focus_session/$taskId"

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Dashboard : BottomNavItem("dashboard", Icons.Rounded.Home, "HOME")
    object Tasks : BottomNavItem("tasks", Icons.Rounded.TaskAlt, "TASKS")
    object Focus : BottomNavItem("focus", Icons.Rounded.GpsFixed, "FOCUS")
    object Notes : BottomNavItem("notes", Icons.Rounded.Assignment, "NOTES")
    object Profile : BottomNavItem("profile", Icons.Rounded.Person, "PROFILE")
}

private val bottomNavRoutes = setOf(
    BottomNavItem.Dashboard.route,
    BottomNavItem.Tasks.route,
    BottomNavItem.Focus.route,
    BottomNavItem.Notes.route,
    BottomNavItem.Profile.route
)

@Composable
fun MainScreen(
    rootNavController: NavHostController
) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide the bottom bar when on the FocusScreen
    val showBottomBar = currentRoute in bottomNavRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(navController = bottomNavController)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = BottomNavItem.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {

            composable(BottomNavItem.Dashboard.route) {
                DashboardScreen(
                    onStartFocus = { taskId ->
                        bottomNavController.navigate(focusRoute(taskId))
                    }
                )
            }
            composable(BottomNavItem.Tasks.route) {
                TaskScreen(
                    onStartFocus = { taskId ->
                        bottomNavController.navigate(focusRoute(taskId))
                    }
                )
            }

            composable(BottomNavItem.Focus.route) {
                FocusScreen()
            }

            composable(BottomNavItem.Notes.route) {
                NotesScreen(
                    onAddNote = { bottomNavController.navigate("add_note") },
                    onNoteClick = { noteId -> bottomNavController.navigate("add_note?noteId=$noteId") }
                )
            }

            composable(BottomNavItem.Profile.route) {
                ProfileScreen(
                    onNavigateToRegulator = { bottomNavController.navigate("regulator") },
                    onNavigateToStreakHistory = { bottomNavController.navigate("streak_history") }
                )
            }

            // Regulator details screen (hides bottom bar)
            composable("regulator") {
                RegulatorScreen(
                    onNavigateBack = { bottomNavController.popBackStack() }
                )
            }

            // Streak History details screen (hides bottom bar)
            composable("streak_history") {
                StreakHistoryScreen(
                    onNavigateBack = { bottomNavController.popBackStack() }
                )
            }
            
            // Add Note screen (hides bottom bar)
            composable(
                route = "add_note?noteId={noteId}",
                arguments = listOf(navArgument("noteId") { type = NavType.StringType; nullable = true; defaultValue = null })
            ) {
                AddNotesScreen(
                    onNavigateBack = { bottomNavController.popBackStack() }
                )
            }
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
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
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
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
        Text(title, color = MaterialTheme.colorScheme.onBackground)
    }
}
