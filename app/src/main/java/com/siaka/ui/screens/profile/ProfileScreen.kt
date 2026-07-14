package com.siaka.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.DarkGray
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siaka.R
import androidx.compose.ui.tooling.preview.Preview
import com.siaka.ui.theme.Background
import com.siaka.ui.theme.DarkNavy
import com.siaka.ui.theme.DangerRed
import com.siaka.ui.theme.SiakaTheme
import com.siaka.ui.theme.LightBlue
import com.siaka.ui.theme.Neutral
import com.siaka.ui.theme.Primary
import com.siaka.ui.theme.PrimaryDark
import com.siaka.ui.theme.PrimaryLight
import com.siaka.ui.theme.Secondary
import com.siaka.ui.theme.Tertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Profile",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold), 
                        color = PrimaryDark,
                        letterSpacing = 0.5.sp
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                ),
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Background)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile Header
            ProfileHeader()

            Spacer(modifier = Modifier.height(19.dp))

            // Account Section
            SectionHeader(title = "Account Settings")
            
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
            ) {
                ProfileOptionItem(
                    icon = ProfileIcon.Painter(painterResource(R.drawable.profile_outlined)),
                    title = "Personal Information",
                    subtitle = "Update your name and email",
                    onClick = {}
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.3f))

                ProfileOptionItem(
                    icon = ProfileIcon.Vector(Icons.Default.History),
                    title = "Ride History",
                    subtitle = "Your previous journeys",
                    onClick = {}
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.3f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Support & Preferences
            SectionHeader(title = "Support & Preferences")
            
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
            ) {
                ProfileOptionItem(
                    icon = ProfileIcon.Vector(Icons.Default.Settings),
                    title = "App Settings",
                    subtitle = "Notification and Appearance",
                    onClick = {}
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.3f))

                ProfileOptionItem(
                    icon = ProfileIcon.Vector(Icons.AutoMirrored.Filled.Help),
                    title = "Help & Support",
                    subtitle = "Get assistance from our team",
                    onClick = {}
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Logout Button
            Button(
                onClick = { /* TODO: Logout */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DangerRed.copy(alpha = 0.1f),
                    contentColor = DangerRed
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = null
            ) {
                Icon(
                    painter = painterResource(R.drawable.log_out),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Log Out", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
            
            Spacer(modifier = Modifier.height(78.dp))
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(start = 32.dp, bottom = 12.dp),
        style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            letterSpacing = 1.sp
        )
    )
}

@Composable
fun ProfileHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            // Profile Picture
            Surface(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                color = LightBlue.copy(alpha = 0.4f),
               // border = BorderStroke(4.dp, Color.White)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = R.drawable.profile_filled),
                        contentDescription = null,
                        modifier = Modifier.size(70.dp),
                        tint = DarkGray.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Edit Button (Pen)
            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .offset(x = (-4).dp, y = (-4).dp)
                    .clip(CircleShape)
                    .clickable { /* TODO: Pick Image */ },
                color = Secondary,
                contentColor = Color.White,
                tonalElevation = 4.dp,
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.update),
                        contentDescription = "Edit Profile Photo",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Text(
            text = "John Doe",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
            color = PrimaryLight
        )
    }
}

@Composable
fun ProfileOptionItem(
    icon: ProfileIcon,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFFF0F2F8)
        ) {
            Box(contentAlignment = Alignment.Center) {
                when (icon) {
                    is ProfileIcon.Vector -> Icon(icon.imageVector, contentDescription = null, tint = Primary, modifier = Modifier.size(22.dp))
                    is ProfileIcon.Painter -> Icon(icon.painter, contentDescription = null, tint = Primary, modifier = Modifier.size(22.dp))
                }
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1.0f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = DarkNavy
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray.copy(alpha = 0.8f)
            )
        }
        
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.size(20.dp)
        )
    }
}

sealed class ProfileIcon {
    data class Vector(val imageVector: ImageVector) : ProfileIcon()
    data class Painter(val painter: androidx.compose.ui.graphics.painter.Painter) : ProfileIcon()
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    SiakaTheme {
        ProfileScreen()
    }
}

