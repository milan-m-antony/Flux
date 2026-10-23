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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
import com.example.ui.theme.NeoTopBar
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
fun SignUpScreen(viewModel: MainViewModel, onSignUpSuccess: () -> Unit, onBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current

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
                        onSignUpSuccess()
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
                errorMessage = e.localizedMessage ?: "Google Sign-Up failed"
            }
        } catch (e: Exception) {
            errorMessage = e.localizedMessage ?: "Google Sign-Up failed"
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "Create Account",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "Join Flux for smart temperature telemetry",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 16.sp
            )
            
            Spacer(modifier = Modifier.height(36.dp))
            
            // Name Input
            Box(modifier = Modifier.fillMaxWidth().neoCard(RoundedCornerShape(20.dp))) {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Full Name", color = MaterialTheme.colorScheme.onSurfaceVariant) },
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
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = AccentCyan) }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
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
            
            Spacer(modifier = Modifier.height(32.dp))
            
            if (isLoading) {
                CircularProgressIndicator(color = AccentCyan, modifier = Modifier.padding(16.dp))
            } else {
                NeoButton(
                    onClick = {
                        if (name.trim().isEmpty() || email.trim().isEmpty() || password.trim().isEmpty()) {
                            errorMessage = "Please fill in all details"
                            return@NeoButton
                        }
                        if (password.trim().length < 6) {
                            errorMessage = "Password must be at least 6 characters"
                            return@NeoButton
                        }
                        isLoading = true
                        errorMessage = null
                        viewModel.signUpWithEmail(email.trim(), password.trim(), name.trim(),
                            onSuccess = {
                                isLoading = false
                                onSignUpSuccess()
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
                    Text("Sign Up", color = AccentCyan, fontWeight = FontWeight.Bold, fontSize = 20.sp)
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
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                Text("  Or sign up with  ", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Google Sign Up Button without background
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
                    Text("Sign up with Google", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                "By signing up, you agree to our Terms and Conditions",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(modifier = Modifier.padding(bottom = 16.dp)) {
                Text("Already have an account? ", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Login", color = AccentCyan, fontWeight = FontWeight.ExtraBold, modifier = Modifier.clickable { onBack() })
            }
        }
    }
}
