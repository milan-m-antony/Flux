package com.example.ui

import android.Manifest
import android.os.Build
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.background
import androidx.compose.runtime.LaunchedEffect
import com.example.ui.AbstractBackground
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.draw.clip
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.MainViewModel
import com.example.ble.ConnectionState
import com.example.ui.DeviceSetupScreen
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.DarkButtonBg
import com.example.ui.theme.TextGrey
import com.example.ui.theme.neoCard
import com.example.ui.theme.glass
import kotlinx.serialization.Serializable

@Serializable
object SplashRoute
@Serializable
object LiveTemperatureRoute
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
object DeviceRoute
@Serializable
object ProfileRoute
@Serializable
object DeviceSetupRoute
@Serializable
object EditProfileRoute
@Serializable
object SettingsRoute
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
    val darkTheme = MaterialTheme.colorScheme.background == Color(0xFF000000)
    val activeAccent = if (darkTheme) AccentCyan else Color(0xFF059669)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val connectionState by viewModel.connectionState.collectAsState()
    val userPhotoUrl by viewModel.userPhotoUrl.collectAsState()

    // Permissions
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

    Box(modifier = Modifier.fillMaxSize()) {
        AbstractBackground()
        
        Scaffold(
            containerColor = Color.Transparent
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize()) {
                NavHost(
                    navController = navController,
                    startDestination = SplashRoute,
                    modifier = Modifier.fillMaxSize()
                ) {
            composable<SplashRoute> {
                SplashScreen(
                    viewModel = viewModel,
                    onNavigateToNext = { isLoggedIn ->
                        if (isLoggedIn) {
                            navController.navigate(HomeRoute) { popUpTo(SplashRoute) { inclusive = true } }
                        } else {
                            navController.navigate(LoginRoute) { popUpTo(SplashRoute) { inclusive = true } }
                        }
                    }
                )
            }
            composable<LoginRoute> {
                LoginScreen(
                    viewModel = viewModel,
                    onLoginSuccess = {
                        navController.navigate(DeviceSetupRoute) { popUpTo(LoginRoute) { inclusive = true } }
                    },
                    onNavigateToSignUp = { navController.navigate(SignUpRoute) }
                )
            }
            composable<SignUpRoute> {
                SignUpScreen(
                    viewModel = viewModel,
                    onSignUpSuccess = {
                        navController.navigate(DeviceSetupRoute) { popUpTo(SignUpRoute) { inclusive = true } }
                    },
                    onBack = { navController.navigateUp() }
                )
            }
            composable<DeviceSetupRoute> {
                DeviceSetupScreen(
                    viewModel = viewModel,
                    onNext = {
                        navController.navigate(HomeRoute) { popUpTo(DeviceSetupRoute) { inclusive = true } }
                    },
                    onSkip = {
                        navController.navigate(HomeRoute) { popUpTo(DeviceSetupRoute) { inclusive = true } }
                    }
                )
            }
            composable<ScanRoute> {
                ScanScreen(
                    viewModel = viewModel,
                    onNavigateToHome = {
                        navController.navigate(HomeRoute) {
                            popUpTo(ScanRoute) { inclusive = true }
                        }
                    },
                    onBack = { navController.navigateUp() }
                )
            }
            composable<HomeRoute> {
                HomeScreen(
                    viewModel = viewModel, 
                    onNavigateToScan = {
                        navController.navigate(ScanRoute)
                    },
                    onNavigateToProfile = {
                        navController.navigate(ProfileRoute) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToNotification = {
                        navController.navigate(NotificationRoute)
                    },
                    onNavigateToLiveTemp = {
                        navController.navigate(FluxLiveMonitorRoute)
                    },
                    onNavigateToEnvironment = {
                        navController.navigate(FluxEnvironmentRoute)
                    },
                    onNavigateToLiquid = {
                        navController.navigate(FluxLiquidRoute)
                    },
                    onNavigateToFermentation = {
                        navController.navigate(FluxFermentationRoute)
                    },
                    onNavigateToStats = {
                        navController.navigate(FluxStatsTimerRoute)
                    },
                    onNavigateToAlerts = {
                        navController.navigate(FluxAlertSettingsRoute)
                    },
                    onNavigateToDiagnostics = {
                        navController.navigate(FluxDiagnosticsRoute)
                    }
                )
            }
            composable<HistoryRoute> {
                HistoryScreen(viewModel, onBack = { navController.navigateUp() })
            }
            composable<DeviceRoute> {
                DeviceScreen(
                    viewModel = viewModel,
                    onNavigateToDiagnostics = {
                        navController.navigate(FluxDiagnosticsRoute)
                    },
                    onNavigateToLiveTemp = {
                        navController.navigate(FluxLiveMonitorRoute)
                    }
                )
            }
            composable<LiveTemperatureRoute> {
                val context = androidx.compose.ui.platform.LocalContext.current
                val application = context.applicationContext as android.app.Application
                val tempViewModel: com.example.ble.BleTemperatureViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = com.example.ble.BleTemperatureViewModel.Factory(application)
                )
                LiveTemperatureScreen(
                    viewModel = tempViewModel,
                    onBack = { navController.navigateUp() }
                )
            }
            composable<ProfileRoute> {
                ProfileScreen(
                    viewModel = viewModel,
                    onNavigateToSettings = {
                        navController.navigate(SettingsRoute)
                    },
                    onNavigateToEditProfile = {
                        navController.navigate(EditProfileRoute)
                    },
                    onNavigateToNotifications = {
                        navController.navigate(NotificationRoute)
                    },
                    onNavigateToAlerts = {
                        navController.navigate(FluxAlertSettingsRoute)
                    },
                    onNavigateToDiagnostics = {
                        navController.navigate(FluxDiagnosticsRoute)
                    },
                    onNavigateToPrivacy = {
                        navController.navigate(PrivacySecurityRoute)
                    },
                    onNavigateToHelpSupport = {
                        navController.navigate(HelpSupportRoute)
                    },
                    onNavigateToAppVersion = {
                        navController.navigate(VersionDetailsRoute)
                    },
                    onNavigateToFcmToken = {
                        navController.navigate(FcmTokenRoute)
                    },
                    onLogout = {
                        viewModel.logout()
                        navController.navigate(LoginRoute) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable<SettingsRoute> {
                SettingsScreen(viewModel, onBack = { navController.navigateUp() })
            }
            composable<EditProfileRoute> {
                EditProfileScreen(viewModel, onBack = { navController.navigateUp() })
            }
            composable<NotificationRoute> {
                NotificationScreen(viewModel, onBack = { navController.navigateUp() })
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
                FluxAlertSettingsScreen(viewModel, onBack = { navController.navigateUp() })
            }
            composable<FluxDiagnosticsRoute> {
                FluxDiagnosticsScreen(viewModel, onBack = { navController.navigateUp() })
            }
        }
        
        // Show bottom bar only on main tabs
        val isMainTab = currentDestination?.hierarchy?.any { 
            it.route?.contains("HomeRoute") == true ||
            it.route?.contains("HistoryRoute") == true ||
            it.route?.contains("FluxLiveMonitorRoute") == true ||
            it.route?.contains("DeviceRoute") == true ||
            it.route?.contains("ProfileRoute") == true
        } == true
        
        if (isMainTab) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .align(androidx.compose.ui.Alignment.BottomCenter)
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .padding(horizontal = 16.dp, vertical = 20.dp)
                    .height(80.dp)
                    .background(
                        color = if (darkTheme) Color.Black else Color(0xFFF3F4F6),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(40.dp)
                    )
                    .border(1.dp, if (darkTheme) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.1f), androidx.compose.foundation.shape.RoundedCornerShape(40.dp))
                    .padding(horizontal = 8.dp)
            ) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    val navItems = listOf(
                        Triple("Home", Icons.Filled.Home, HomeRoute),
                        Triple("History", Icons.AutoMirrored.Filled.List, HistoryRoute),
                        Triple("Monitor", Icons.Filled.Thermostat, FluxLiveMonitorRoute),
                        Triple("Device", Icons.Filled.Bluetooth, DeviceRoute),
                        Triple("Profile", Icons.Filled.Person, ProfileRoute)
                    )
                    
                    navItems.forEach { (label, icon, route) ->
                        val selected = currentDestination?.hierarchy?.any { it.route?.contains(route::class.simpleName ?: "") == true } == true
                        
                        androidx.compose.foundation.layout.Box(
                            contentAlignment = androidx.compose.ui.Alignment.Center,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (route == FluxLiveMonitorRoute) {
                                // Special styling for center FAB
                                com.example.ui.theme.NeoButton(
                                    onClick = {
                                        navController.navigate(route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    shape = androidx.compose.foundation.shape.CircleShape,
                                    modifier = Modifier.size(60.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        tint = if (selected) activeAccent else (if (darkTheme) MaterialTheme.colorScheme.onSurface else Color(0xFF111827)),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            } else {
                                if (selected) {
                                    com.example.ui.theme.NeoButton(
                                        onClick = {
                                            navController.navigate(route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        shape = androidx.compose.foundation.shape.CircleShape,
                                        modifier = Modifier.size(48.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                    ) {
                                        if (route == ProfileRoute) {
                                            if (userPhotoUrl.isNotEmpty()) {
                                                AsyncImage(
                                                    model = userPhotoUrl,
                                                    contentDescription = label,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(CircleShape)
                                                        .border(2.dp, activeAccent, CircleShape)
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = label,
                                                    tint = activeAccent,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        } else {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = label,
                                                tint = activeAccent,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                } else {
                                    androidx.compose.material3.IconButton(
                                        onClick = {
                                            navController.navigate(route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        if (route == ProfileRoute) {
                                            if (userPhotoUrl.isNotEmpty()) {
                                                AsyncImage(
                                                    model = userPhotoUrl,
                                                    contentDescription = label,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .clip(CircleShape)
                                                        .border(1.5.dp, if (darkTheme) Color.White.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.2f), CircleShape)
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = label,
                                                    tint = if (darkTheme) TextGrey else Color(0xFF4B5563),
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        } else {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = label,
                                                tint = if (darkTheme) TextGrey else Color(0xFF4B5563),
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun NaTrackApp(viewModel: MainViewModel) {
    FluxApp(viewModel)
}
}
