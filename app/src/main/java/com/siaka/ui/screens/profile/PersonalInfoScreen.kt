package com.siaka.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.siaka.ui.screens.auth.AuthTextField
import com.siaka.ui.theme.DarkNavy
import com.siaka.ui.theme.PrimaryDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInfoScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val currentName by viewModel.userName.collectAsState()
    val currentEmail by viewModel.userEmail.collectAsState()

    var name by remember(currentName) { mutableStateOf(currentName) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Personal Information",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryDark
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            Text(
                "Update your profile details below.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            AuthTextField(
                value = name,
                onValueChange = { name = it },
                label = "Full Name",
                leadingIcon = Icons.Default.Person
            )

            Spacer(modifier = Modifier.height(16.dp))

            AuthTextField(
                value = currentEmail,
                onValueChange = { },
                label = "Email Address",
                leadingIcon = Icons.Default.Email,
                enabled = false
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { 
                    viewModel.updatePersonalInformation(name)
                    viewModel.fetchUserProfile() // Force immediate refresh of internal state
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkNavy)
            ) {
                Text(
                    "Save Changes",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
