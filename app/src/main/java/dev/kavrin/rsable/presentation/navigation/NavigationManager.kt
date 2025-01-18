package dev.kavrin.rsable.presentation.navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import dev.kavrin.rsable.presentation.screens.client.BleClientContract
import dev.kavrin.rsable.presentation.screens.client.BleClientViewModel
import dev.kavrin.rsable.presentation.screens.client.ble_detail.BleDetailContract
import dev.kavrin.rsable.presentation.screens.client.ble_detail.BleDetailScreenRoot
import dev.kavrin.rsable.presentation.screens.client.ble_detail.BleDetailViewModel
import dev.kavrin.rsable.presentation.screens.client.ble_list.BleListScreenRoot
import dev.kavrin.rsable.presentation.screens.client.ble_list.BleListViewModel
import dev.kavrin.rsable.presentation.screens.intro.IntroScreenRoot
import dev.kavrin.rsable.presentation.screens.intro.IntroViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun NavigationManager(
    modifier: Modifier = Modifier,
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

                },
                onNavigateToCentral = {
                    navController.navigate(
                        Route.ClientGraph
                    )
                }
            )
        }

        navigation<Route.ClientGraph>(
            startDestination = Route.ClientBleList
        ) {
            composable<Route.ClientBleList>(
                exitTransition = {
                    slideOutHorizontally()
                },
                popEnterTransition = {
                    slideInHorizontally()
                }
            ) { navBackStackEntry ->

                val clientViewModel =
                    navBackStackEntry.sharedKoinViewModel<BleClientViewModel>(navController)
                val viewModel = koinViewModel<BleListViewModel>()

                LaunchedEffect(Unit) {
                    clientViewModel.onEvent(
                        BleClientContract.Event.OnSelectedBleChange(
                            null,
                            emptyList()
                        )
                    )
                }

                BleListScreenRoot(
                    viewModel = viewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToDetail = { bleDevice, gattServices ->
                        clientViewModel.onEvent(
                            BleClientContract.Event.OnSelectedBleChange(
                                bleDevice,
                                gattServices
                            )
                        )
                        navController.navigate(
                            Route.ClientBleDetail
                        )
                    }
                )
            }

            composable<Route.ClientBleDetail>(
                exitTransition = {
                    slideOutHorizontally()
                },
                popEnterTransition = {
                    slideInHorizontally()
                }
            ) { navBackStackEntry ->

                val clientViewModel =
                    navBackStackEntry.sharedKoinViewModel<BleClientViewModel>(navController)
                val viewModel = koinViewModel<BleDetailViewModel>()

                val clientState = clientViewModel.state.collectAsStateWithLifecycle()

                LaunchedEffect(clientState) {
                    viewModel.onEvent(
                        BleDetailContract.Event.OnSelectedBleChange(clientState.value.bleDevice)
                    )
                }

                BleDetailScreenRoot(
                    viewModel = viewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }

}

@Composable
private inline fun <reified T : ViewModel> NavBackStackEntry.sharedKoinViewModel(
    navController: NavController,
): T {
    val navGraphRoute = destination.parent?.route ?: return koinViewModel<T>()
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }

    // Scope viewmodel to the parent graph
    return koinViewModel(
        viewModelStoreOwner = parentEntry
    )

}