package com.andytools.retroclub.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.andytools.retroclub.ui.home.HomeScreen
import com.andytools.retroclub.ui.media.player.MediaPlayerViewModel
import com.andytools.retroclub.ui.theme.ThemeManager

@Composable
fun NavGraph(
    viewModel: MediaPlayerViewModel,
    themeManager: ThemeManager
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(viewModel = viewModel, themeManager = themeManager)
        }
    }
}