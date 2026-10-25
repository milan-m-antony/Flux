package com.example.ui

import android.Manifest
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.components.FluxQuickWifiSheet
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.MainViewModel
import com.example.ui.theme.AccentCyan
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.serialization.Serializable

@Serializable
object SplashRoute
@Serializable
object LoginRoute
@Serializable
object SignUpRoute
@Serializable
object ScanRoute
@Serializable
object HomeRoute
@Serializable
object HistoryRoute
@Serializable
object ProfilesRoute
@Serializable
object AlertsRoute
@Serializable
object SettingsRoute
@Serializable
object DeviceRoute
@Serializable
object ProfileRoute
@Serializable
object DeviceSetupRoute
@Serializable
object NotificationRoute
@Serializable
object PrivacySecurityRoute
@Serializable
object HelpSupportRoute
@Serializable
object VersionDetailsRoute
@Serializable
object FcmTokenRoute
@Serializable
object FluxLiveMonitorRoute
@Serializable
object FluxEnvironmentRoute
@Serializable
object FluxLiquidRoute
@Serializable
object FluxFermentationRoute
@Serializable
object FluxStatsTimerRoute
@Serializable
object FluxAlertSettingsRoute
@Serializable
object FluxDiagnosticsRoute

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun FluxApp(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Runtime Permissions
    val permissions = mutableListOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        permissions.add(Manifest.permission.BLUETOOTH_SCAN)
        permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissions.add(Manifest.permission.POST_NOTIFICATIONS)
    }

    val permissionState = rememberMultiplePermissionsState(permissions = permissions)

    LaunchedEffect(Unit) {
        if (!permissionState.allPermissionsGranted) {
            permissionState.launchMultiplePermissionRequest()
        }
    }

    // Determine current active main screen for bottom navigation
    val currentRoute = currentDestination?.route
    val activeScreen: FluxScreen? = when {
        currentRoute?.contains("HomeRoute") == true -> FluxScreen.HOME
        currentRoute?.contains("HistoryRoute") == true -> FluxScreen.HISTORY
        currentRoute?.contains("ProfilesRoute") == true || currentRoute?.contains("ProfileRoute") == true -> FluxScreen.PROFILES
        currentRoute?.contains("AlertsRoute") == true || currentRoute?.contains("NotificationRoute") == true -> FluxScreen.ALERTS
        currentRoute?.contains("SettingsRoute") == true -> FluxScreen.SETTINGS
        else -> null
    }

    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val connectivityState by viewModel.connectivityState.collectAsState()
    val isOnline = connectivityState.isOnline
    val allNotifications by viewModel.allNotifications.collectAsState()
    val hasUnreadAlerts = allNotifications.any { !it.isRead }

    var showWifiSetupSheet by remember { mutableStateOf(false) }

    FluxBackground(darkTheme = isDarkTheme) {
        Scaffold(
            containerColor = Color.Transparent
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize()) {
                NavHost(
                    navController = navController,
                    startDestination = HomeRoute,
                    modifier = Modifier.fillMaxSize(),
                    enterTransition = { androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(240)) },
                    exitTransition = { androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(180)) },
                    popEnterTransition = { androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(240)) },
                    popExitTransition = { androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(180)) }
                ) {
                    // 1. Screen 1 — Home (Exact match to Reference Screenshot 1)
                    composable<HomeRoute> {
                        FluxHomeScreen(
                            viewModel = viewModel,
                            onNavigateToHistory = {
                                navController.navigate(HistoryRoute) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onNotificationClick = {
                                navController.navigate(AlertsRoute) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onNavigateToDeviceSetup = {
                                navController.navigate(DeviceSetupRoute)
                            }
                        )
                    }

                    // 2. Screen 2 — History (Exact match to Reference Screenshot 2)
                    composable<HistoryRoute> {
                        FluxHistoryScreen(
                            viewModel = viewModel,
                            onNotificationClick = {
                                navController.navigate(AlertsRoute) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }

                    // 3. Screen 3 — Profiles (Exact match to Reference Screenshot 3)
                    composable<ProfilesRoute> {
                        FluxProfilesScreen(
                            viewModel = viewModel,
                            onBack = { navController.navigateUp() },
                            onNotificationClick = {
                                navController.navigate(AlertsRoute) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }

                    // Legacy route alias for profile
                    composable<ProfileRoute> {
                        FluxProfilesScreen(
                            viewModel = viewModel,
                            onBack = { navController.navigateUp() },
                            onNotificationClick = {
                                navController.navigate(AlertsRoute)
                            }
                        )
                    }

                    // 4. Screen 4 — Alerts (Exact match to Reference Screenshot 4)
                    composable<AlertsRoute> {
                        FluxAlertsScreen(
                            viewModel = viewModel,
                            onNotificationClick = {
                                navController.navigate(AlertsRoute)
                            }
                        )
                    }

                    // Legacy route alias for notifications
                    composable<NotificationRoute> {
                        FluxAlertsScreen(
                            viewModel = viewModel,
                            onNotificationClick = {
                                navController.navigate(AlertsRoute)
                            }
                        )
                    }

                    // 5. Screen 5 — Settings (Exact match to Reference Screenshot 5)
                    composable<SettingsRoute> {
                        FluxSettingsScreen(
                            viewModel = viewModel,
                            onNotificationClick = {
                                navController.navigate(AlertsRoute)
                            },
                            onNavigateToDevice = {
                                navController.navigate(DeviceRoute)
                            },
                            onNavigateToWifiSetup = {
                                showWifiSetupSheet = true
                            }
                        )
                    }

                    // Retained sub-screens and flows
                    composable<SplashRoute> {
                        SplashScreen(
                            viewModel = viewModel,
                            onNavigateToNext = { isLoggedIn ->
                                navController.navigate(HomeRoute) { popUpTo(SplashRoute) { inclusive = true } }
                            }
                        )
                    }
                    composable<LoginRoute> {
                        LoginScreen(
                            viewModel = viewModel,
                            onLoginSuccess = {
                                navController.navigate(HomeRoute) { popUpTo(LoginRoute) { inclusive = true } }
                            },
                            onNavigateToSignUp = { navController.navigate(SignUpRoute) }
                        )
                    }
                    composable<SignUpRoute> {
                        SignUpScreen(
                            viewModel = viewModel,
                            onSignUpSuccess = {
                                navController.navigate(HomeRoute) { popUpTo(SignUpRoute) { inclusive = true } }
                            },
                            onBack = { navController.navigateUp() }
                        )
                    }
                    composable<DeviceSetupRoute> {
                        DeviceSetupScreen(
                            viewModel = viewModel,
                            onNext = { navController.navigate(HomeRoute) { popUpTo(DeviceSetupRoute) { inclusive = true } } },
                            onSkip = { navController.navigate(HomeRoute) { popUpTo(DeviceSetupRoute) { inclusive = true } } },
                            onBack = { navController.navigateUp() }
                        )
                    }
                    composable<ScanRoute> {
                        ScanScreen(
                            viewModel = viewModel,
                            onNavigateToHome = {
                                navController.navigate(HomeRoute) { popUpTo(ScanRoute) { inclusive = true } }
                            },
                            onBack = { navController.navigateUp() }
                        )
                    }
                    composable<DeviceRoute> {
                        FluxDeviceScreen(
                            viewModel = viewModel,
                            onBack = { navController.navigateUp() },
                            onNavigateToDiagnostics = { navController.navigate(FluxDiagnosticsRoute) },
                            onNavigateToWifiSetup = { showWifiSetupSheet = true }
                        )
                    }
                    composable<FluxLiveMonitorRoute> {
                        FluxLiveMonitorScreen(viewModel, onBack = { navController.navigateUp() })
                    }
                    composable<FluxEnvironmentRoute> {
                        FluxEnvironmentScreen(viewModel, onBack = { navController.navigateUp() })
                    }
                    composable<FluxLiquidRoute> {
                        FluxLiquidScreen(viewModel, onBack = { navController.navigateUp() })
                    }
                    composable<FluxFermentationRoute> {
                        FluxFermentationScreen(viewModel, onBack = { navController.navigateUp() })
                    }
                    composable<FluxStatsTimerRoute> {
                        FluxStatsTimerScreen(viewModel, onBack = { navController.navigateUp() })
                    }
                    composable<FluxAlertSettingsRoute> {
                        FluxAlertsScreen(viewModel)
                    }
                    composable<FluxDiagnosticsRoute> {
                        FluxDiagnosticsScreen(viewModel, onBack = { navController.navigateUp() })
                    }
                    composable<PrivacySecurityRoute> {
                        PrivacySecurityScreen(onBack = { navController.navigateUp() })
                    }
                    composable<HelpSupportRoute> {
                        HelpSupportScreen(onBack = { navController.navigateUp() })
                    }
                    composable<VersionDetailsRoute> {
                        VersionDetailsScreen(onBack = { navController.navigateUp() })
                    }
                    composable<FcmTokenRoute> {
                        FcmTokenScreen(onBack = { navController.navigateUp() })
                    }
                }

                // Shared Fixed Top Header (Rendered permanently on all 5 main screens)
                if (activeScreen != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(start = 16.dp, top = 2.dp, end = 16.dp, bottom = 4.dp)
                            .align(Alignment.TopCenter)
                    ) {
                        FluxHeader(
                            connectivityState = connectivityState,
                            isOnline = isOnline,
                            isBleConnected = connectionState == com.example.ble.ConnectionState.CONNECTED,
                            isWifiConnected = connectivityState.type == com.example.connectivity.ConnectionType.WIFI,
                            darkTheme = isDarkTheme,
                            hasUnreadAlerts = hasUnreadAlerts,
                            onNotificationClick = {
                                navController.navigate(AlertsRoute) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onDeviceClick = {
                                navController.navigate(DeviceRoute)
                            }
                        )
                    }
                }

                // Shared Floating Bottom Navigation Bar (Rendered on all 5 main screens)
                if (activeScreen != null) {
                    FluxBottomNavigation(
                        currentScreen = activeScreen,
                        onScreenSelected = { screen ->
                            val targetRoute = when (screen) {
                                FluxScreen.HOME -> HomeRoute
                                FluxScreen.HISTORY -> HistoryRoute
                                FluxScreen.PROFILES -> ProfilesRoute
                                FluxScreen.ALERTS -> AlertsRoute
                                FluxScreen.SETTINGS -> SettingsRoute
                            }
                            navController.navigate(targetRoute) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.align(Alignment.BottomCenter),
                        darkTheme = isDarkTheme
                    )
                }

                // Automated 1-Step Quick Wi-Fi Setup Sheet
                if (showWifiSetupSheet) {
                    FluxQuickWifiSheet(
                        viewModel = viewModel,
                        onDismiss = { showWifiSetupSheet = false }
                    )
                }
            }
        }
    }
}
