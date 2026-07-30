package com.example.atrox

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.atrox.ui.navigation.AtroxNavHost

@RequiresApi(Build.VERSION_CODES.O)
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