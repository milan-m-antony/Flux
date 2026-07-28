package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.NeoButton
import com.example.ui.theme.TextGrey
import com.example.ui.theme.neoCard
import com.example.MainViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun LoginScreen(viewModel: MainViewModel, onLoginSuccess: () -> Unit, onNavigateToSignUp: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    var showResetDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var resetSuccessMessage by remember { mutableStateOf<String?>(null) }
    var resetErrorMessage by remember { mutableStateOf<String?>(null) }
    var isResetLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var showBiometricSetupDialog by remember { mutableStateOf(false) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken != null) {
                isLoading = true
                viewModel.loginWithGoogleToken(idToken,
                    onSuccess = {
                        isLoading = false
                        if (!com.example.util.BiometricHelper.isAppLockEnabled(context) && com.example.util.BiometricHelper.isBiometricAvailable(context)) {
                            showBiometricSetupDialog = true
                        } else {
                            onLoginSuccess()
                        }
                    },
                    onFailure = { err ->
                        isLoading = false
                        errorMessage = err
                    }
                )
            } else {
                errorMessage = "Google ID Token was null"
            }
        } catch (e: ApiException) {
            if (e.statusCode != 12501) {
                errorMessage = e.localizedMessage ?: "Google Sign-In failed"
            }
        } catch (e: Exception) {
            errorMessage = e.localizedMessage ?: "Google Sign-In failed"
        }
    }

    fun triggerGoogleSignIn() {
        val webClientIdResId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        val webClientId = if (webClientIdResId != 0) context.getString(webClientIdResId) else null
        
        if (webClientId == null) {
            errorMessage = "Google Sign-In configuration (Web Client ID) is missing in your Firebase configuration."
            return
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        googleSignInLauncher.launch(googleSignInClient.signInIntent)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { 
                    if (!isResetLoading) {
                        showResetDialog = false 
                        resetSuccessMessage = null
                        resetErrorMessage = null
                    }
                },
                title = {
                    Text(
                        text = "Reset Password",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Enter your email address and we'll send you a password reset link.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                        
                        Box(modifier = Modifier.fillMaxWidth().neoCard(RoundedCornerShape(12.dp))) {
                            TextField(
                                value = resetEmail,
                                onValueChange = { resetEmail = it },
                                placeholder = { Text("Email Address", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isResetLoading && resetSuccessMessage == null,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = AccentCyan,
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                ),
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = AccentCyan) }
                            )
                        }

                        if (resetSuccessMessage != null) {
                            Text(
                                text = resetSuccessMessage ?: "",
                                color = AccentCyan,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }

                        if (resetErrorMessage != null) {
                            Text(
                                text = resetErrorMessage ?: "",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                },
                confirmButton = {
                    if (resetSuccessMessage != null) {
                        Button(
                            onClick = {
                                showResetDialog = false
                                resetSuccessMessage = null
                                resetErrorMessage = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
                        ) {
                            Text("Done", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { 
                                    showResetDialog = false 
                                    resetErrorMessage = null
                                },
                                enabled = !isResetLoading
                            ) {
                                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            if (isResetLoading) {
                                CircularProgressIndicator(
                                    color = AccentCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Button(
                                    onClick = {
                                        if (resetEmail.trim().isEmpty()) {
                                            resetErrorMessage = "Please enter your email address"
                                            return@Button
                                        }
                                        isResetLoading = true
                                        resetErrorMessage = null
                                        viewModel.sendPasswordResetEmail(
                                            emailStr = resetEmail.trim(),
                                            onSuccess = {
                                                isResetLoading = false
                                                resetSuccessMessage = "Password reset email sent! Check your inbox."
                                            },
                                            onFailure = { err ->
                                                isResetLoading = false
                                                resetErrorMessage = err
                                            }
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
                                ) {
                                    Text("Send Link", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(24.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            NaTLogo(modifier = Modifier.padding(16.dp))
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "Welcome Back",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "Sign in to your NaTrack account",
                color = TextGrey,
                fontSize = 16.sp
            )
            
            Spacer(modifier = Modifier.height(36.dp))
            
            // Email Input
            Box(modifier = Modifier.fillMaxWidth().neoCard(RoundedCornerShape(20.dp))) {
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("Email Address", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = AccentCyan,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = AccentCyan) }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Password Input
            Box(modifier = Modifier.fillMaxWidth().neoCard(RoundedCornerShape(20.dp))) {
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Password", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = AccentCyan,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AccentCyan) }
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text("Forgot Password?", color = AccentCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable {
                    resetEmail = email
                    resetSuccessMessage = null
                    resetErrorMessage = null
                    showResetDialog = true
                })
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            if (isLoading) {
                CircularProgressIndicator(color = AccentCyan, modifier = Modifier.padding(16.dp))
            } else {
                NeoButton(
                    onClick = {
                        if (email.trim().isEmpty() || password.trim().isEmpty()) {
                            errorMessage = "Please enter both email and password"
                            return@NeoButton
                        }
                        isLoading = true
                        errorMessage = null
                        viewModel.loginWithEmail(email.trim(), password.trim(),
                            onSuccess = {
                                isLoading = false
                                if (!com.example.util.BiometricHelper.isAppLockEnabled(context) && com.example.util.BiometricHelper.isBiometricAvailable(context)) {
                                    showBiometricSetupDialog = true
                                } else {
                                    onLoginSuccess()
                                }
                            },
                            onFailure = { err ->
                                isLoading = false
                                errorMessage = err
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Login", color = AccentCyan, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                Text("  Or continue with  ", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Google Login Button without background
                Row(
                    modifier = Modifier
                        .clickable { triggerGoogleSignIn() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = "Google",
                        modifier = Modifier.size(24.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Sign in with Google", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Row(modifier = Modifier.padding(bottom = 16.dp)) {
                Text("Don't have an account? ", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Sign Up", color = AccentCyan, fontWeight = FontWeight.ExtraBold, modifier = Modifier.clickable { onNavigateToSignUp() })
            }
        }
        
        if (showBiometricSetupDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showBiometricSetupDialog = false
                    onLoginSuccess() 
                },
                title = { Text("App Lock") },
                text = { Text("Do you want to enable Biometric (Fingerprint) lock to secure your app?") },
                confirmButton = {
                    TextButton(onClick = {
                        showBiometricSetupDialog = false
                        val activity = context as? androidx.fragment.app.FragmentActivity
                        if (activity != null && com.example.util.BiometricHelper.isBiometricAvailable(activity)) {
                            com.example.util.BiometricHelper.promptBiometricAuth(
                                activity = activity,
                                title = "Setup App Lock",
                                subtitle = "Verify your fingerprint to enable App Lock",
                                onSuccess = {
                                    com.example.util.BiometricHelper.setAppLockEnabled(context, true)
                                    android.widget.Toast.makeText(context, "App Lock Enabled", android.widget.Toast.LENGTH_SHORT).show()
                                    onLoginSuccess()
                                },
                                onFailed = {
                                    android.widget.Toast.makeText(context, "Authentication Failed", android.widget.Toast.LENGTH_SHORT).show()
                                    onLoginSuccess()
                                },
                                onError = { err ->
                                    android.widget.Toast.makeText(context, "Error: $err", android.widget.Toast.LENGTH_SHORT).show()
                                    onLoginSuccess()
                                }
                            )
                        } else {
                            android.widget.Toast.makeText(context, "Biometric not available", android.widget.Toast.LENGTH_SHORT).show()
                            onLoginSuccess()
                        }
                    }) {
                        Text("Enable")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showBiometricSetupDialog = false
                        onLoginSuccess()
                    }) {
                        Text("Not Now")
                    }
                }
            )
        }
    }
}

@Composable
fun SocialLoginButton(modifier: Modifier = Modifier, text: String, icon: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .height(56.dp)
            .neoCard(RoundedCornerShape(16.dp))
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            icon()
            Spacer(modifier = Modifier.width(12.dp))
            Text(text, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        }
    }
}
