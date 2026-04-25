package com.example.atrox

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.atrox.navigation.AtroxNavHost

@Composable
fun AtroxApp(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier
){
    AtroxNavHost(
        navController = navController,
        modifier = modifier
    )
}