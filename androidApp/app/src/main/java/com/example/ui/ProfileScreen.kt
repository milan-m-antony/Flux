package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.MainViewModel
import com.example.R
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.NeoButton
import com.example.ui.theme.NeoIconButton
import com.example.ui.theme.NeoTopBar
import com.example.ui.theme.TextGrey
import com.example.ui.theme.neoCard

@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToAlerts: () -> Unit = {},
    onNavigateToDiagnostics: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit,
    onNavigateToHelpSupport: () -> Unit,
    onNavigateToAppVersion: () -> Unit,
    onNavigateToFcmToken: () -> Unit,
    onLogout: () -> Unit
) {
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val darkTheme = MaterialTheme.colorScheme.background == Color(0xFF000000)
    val activeAccent = if (darkTheme) AccentCyan else Color(0xFF059669)
    
    val name by viewModel.userName.collectAsState()
    val age by viewModel.userAge.collectAsState()
    val weight by viewModel.userWeight.collectAsState()
    val height by viewModel.userHeight.collectAsState()
    val photoUrl by viewModel.userPhotoUrl.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val email = currentUser?.email ?: "milanmantony2027@mca.ajce.in"
    
    var showPhotoOptions by remember { mutableStateOf(false) }
    
    if (showPhotoOptions) {
        AlertDialog(
            onDismissRequest = { showPhotoOptions = false },
            title = { Text("Profile Photo") },
            text = {
                Text("Remove your profile photo?")
            },
            confirmButton = {
                TextButton(onClick = { 
                    viewModel.saveUserProfile(name, age, weight, height, "")
                    showPhotoOptions = false 
                }) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPhotoOptions = false }) {
                    Text("Cancel", color = activeAccent)
                }
            }
        )
    }
    
    Scaffold(
        containerColor = Color.Transparent,
        topBar = { 
            NeoTopBar(
                title = "Profile",
                actions = {}
            ) 
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Header
            Box(
                modifier = Modifier
                    .size(130.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .border(2.dp, activeAccent, CircleShape)
                        .padding(4.dp)
                        .clip(CircleShape)
                        .clickable { showPhotoOptions = true }
                ) {
                    if (photoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = "Profile",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            modifier = Modifier.fillMaxSize().padding(8.dp),
                            tint = activeAccent
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(name, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(email, fontSize = 14.sp, color = TextGrey)
            
            Spacer(modifier = Modifier.height(16.dp))
 
            NeoButton(
                onClick = { onNavigateToEditProfile() },
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text("Edit Profile", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Health Stats Overview
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier.weight(1f).neoCard().padding(16.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("$weight kg", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Weight", fontSize = 12.sp, color = TextGrey)
                    }
                }
                Box(modifier = Modifier.weight(1f).neoCard().padding(16.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("$height cm", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Height", fontSize = 12.sp, color = TextGrey)
                    }
                }
                Box(modifier = Modifier.weight(1f).neoCard().padding(16.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(age, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Age", fontSize = 12.sp, color = TextGrey)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Profile Actions
            val context = LocalContext.current
            var appLockEnabled by remember { mutableStateOf(com.example.util.BiometricHelper.isAppLockEnabled(context)) }

            ProfileActionItem(
                icon = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode, 
                label = "Appearance", 
                onClick = { viewModel.saveTheme(!isDarkTheme) },
                trailing = {
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { viewModel.saveTheme(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = activeAccent,
                            checkedTrackColor = activeAccent.copy(alpha = 0.5f)
                        )
                    )
                }
            )
            ProfileActionItem(
                icon = Icons.Default.Fingerprint, 
                label = "App Lock (Fingerprint)", 
                onClick = { 
                    if (appLockEnabled) {
                        com.example.util.BiometricHelper.setAppLockEnabled(context, false)
                        appLockEnabled = false
                    } else {
                        if (com.example.util.BiometricHelper.isBiometricAvailable(context)) {
                            com.example.util.BiometricHelper.setAppLockEnabled(context, true)
                            appLockEnabled = true
                        } else {
                            android.widget.Toast.makeText(context, "Biometric auth not available", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                trailing = {
                    Switch(
                        checked = appLockEnabled,
                        onCheckedChange = { checked ->
                            if (checked) {
                                if (com.example.util.BiometricHelper.isBiometricAvailable(context)) {
                                    com.example.util.BiometricHelper.setAppLockEnabled(context, true)
                                    appLockEnabled = true
                                } else {
                                    android.widget.Toast.makeText(context, "Biometric auth not available", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                com.example.util.BiometricHelper.setAppLockEnabled(context, false)
                                appLockEnabled = false
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = activeAccent,
                            checkedTrackColor = activeAccent.copy(alpha = 0.5f)
                        )
                    )
                }
            )
            ProfileActionItem(icon = Icons.Default.Notifications, label = "Threshold Alerts & Limits", onClick = onNavigateToAlerts)
            ProfileActionItem(icon = Icons.Default.Info, label = "System Diagnostics", onClick = onNavigateToDiagnostics)
            ProfileActionItem(icon = Icons.Default.Notifications, label = "Notifications List", onClick = onNavigateToNotifications)
            ProfileActionItem(icon = Icons.Default.Settings, label = "App Settings", onClick = onNavigateToSettings)
            ProfileActionItem(icon = Icons.Default.Security, label = "Privacy & Security", onClick = onNavigateToPrivacy)
            ProfileActionItem(icon = Icons.Default.Help, label = "Help & Support", onClick = onNavigateToHelpSupport)
            ProfileActionItem(icon = Icons.Default.Info, label = "App Version Details", onClick = onNavigateToAppVersion)
            ProfileActionItem(icon = Icons.Default.Warning, label = "FCM Token (Test)", onClick = onNavigateToFcmToken)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            NeoButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Logout", color = com.example.ui.theme.DangerRed, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "NaTrack v1.0.4", 
                fontSize = 12.sp, 
                color = TextGrey,
                modifier = Modifier.clickable { onNavigateToAppVersion() }
            )
            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}

@Composable
fun ProfileActionItem(icon: ImageVector, label: String, onClick: () -> Unit, trailing: @Composable (() -> Unit)? = null) {
    val darkTheme = MaterialTheme.colorScheme.background == Color(0xFF000000)
    val activeAccent = if (darkTheme) AccentCyan else Color(0xFF059669)
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).neoCard().clickable { onClick() }.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = activeAccent, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(label, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            if (trailing != null) {
                trailing()
            } else {
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextGrey)
            }
        }
    }
}
