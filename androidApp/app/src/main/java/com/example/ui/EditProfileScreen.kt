package com.example.ui

import android.widget.Toast
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.NeoButton
import com.example.ui.theme.NeoIconButton
import com.example.ui.theme.NeoTopBar
import com.example.ui.theme.TextGrey

@Composable
fun EditProfileScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val currentName by viewModel.userName.collectAsState()
    val currentWeight by viewModel.userWeight.collectAsState()
    val currentHeight by viewModel.userHeight.collectAsState()
    val currentAge by viewModel.userAge.collectAsState()
    val currentPhotoUrl by viewModel.userPhotoUrl.collectAsState()

    var name by remember(currentName) { mutableStateOf(currentName) }
    var weight by remember(currentWeight) { mutableStateOf(currentWeight) }
    var height by remember(currentHeight) { mutableStateOf(currentHeight) }
    var age by remember(currentAge) { mutableStateOf(currentAge) }
    
    var showPhotoOptions by remember { mutableStateOf(false) }
    var photoUrl by remember(currentPhotoUrl) { mutableStateOf(currentPhotoUrl) }
    
    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            Toast.makeText(context, "Uploading...", Toast.LENGTH_SHORT).show()
            viewModel.uploadProfilePicture(uri, context) { url ->
                if (url != null) {
                    photoUrl = url
                    viewModel.saveUserProfile(name, age, weight, height, url)
                    Toast.makeText(context, "Photo uploaded", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { 
            NeoTopBar(
                title = "Edit Profile",
                onBackClick = onBack
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
            // Profile Photo
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .border(2.dp, AccentCyan, CircleShape)
                    .padding(4.dp)
                    .clip(CircleShape)
            ) {
                AsyncImage(
                    model = photoUrl.ifEmpty { null },
                    contentDescription = "Profile Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                NeoButton(
                    onClick = { imageLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Upload")
                    }
                }
                NeoButton(
                    onClick = { 
                        photoUrl = ""
                        viewModel.saveUserProfile(name, age, weight, height, "")
                        Toast.makeText(context, "Photo deleted", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = name, 
                onValueChange = { name = it }, 
                label = { Text("Name") }, 
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = weight, 
                    onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) weight = it }, 
                    label = { Text("Weight (kg)") }, 
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = height, 
                    onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) height = it }, 
                    label = { Text("Height (cm)") }, 
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = age, 
                onValueChange = { if (it.all { char -> char.isDigit() }) age = it }, 
                label = { Text("Age") }, 
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            NeoButton(
                onClick = { 
                    viewModel.saveUserProfile(name, age, weight, height, photoUrl)
                    Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                    onBack() 
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        }
    }
}
