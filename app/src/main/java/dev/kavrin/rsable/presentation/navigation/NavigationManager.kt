package dev.kavrin.rsable.presentation.navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import dev.kavrin.rsable.presentation.screens.intro.IntroScreenRoot
import dev.kavrin.rsable.presentation.screens.intro.IntroViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun NavigationManager(
    modifier: Modifier = Modifier
) {

    val navController = rememberNavController()

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Route.Intro
    ) {
        composable<Route.Intro>(
            exitTransition = {
                slideOutHorizontally()
            },
            popEnterTransition = {
                slideInHorizontally()
            }
        ) { navBackStackEntry ->
            val viewModel = koinViewModel<IntroViewModel>()
            IntroScreenRoot(
                viewModel = viewModel,
                onNavigateToPeripheral = {
                    navController.navigate(
                        Route.PeripheralGraph
                    )
                },
                onNavigateToCentral = {/* TODO:  */}
            )
        }

//        navigation<Route.PeripheralGraph>(
//            startDestination = Route.PeripheralList
//        ) {
//
//        }
    }

}