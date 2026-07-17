package com.siaka.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siaka.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    var darkModeEnabled by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var locationSharingEnabled by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "App Settings",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDark
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PrimaryDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Appearance Section
            SettingsSection(title = "Appearance") {
                SettingsSwitchItem(
                    icon = Icons.Default.DarkMode,
                    title = "Dark Mode",
                    subtitle = "Adjust app theme to your preference",
                    checked = darkModeEnabled,
                    onCheckedChange = { darkModeEnabled = it }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.3f))
                SettingsClickableItem(
                    icon = Icons.Default.Language,
                    title = "Language",
                    subtitle = "English (US)",
                    onClick = {}
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Notifications Section
            SettingsSection(title = "Notifications") {
                SettingsSwitchItem(
                    icon = Icons.Default.Notifications,
                    title = "Push Notifications",
                    subtitle = "Receive alerts about your rides",
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Privacy Section
            SettingsSection(title = "Privacy & Security") {
                SettingsSwitchItem(
                    icon = Icons.Default.LocationOn,
                    title = "Location Sharing",
                    subtitle = "Help us improve your ride accuracy",
                    checked = locationSharingEnabled,
                    onCheckedChange = { locationSharingEnabled = it }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.3f))
                SettingsClickableItem(
                    icon = Icons.Default.Security,
                    title = "Privacy Policy",
                    subtitle = "How we handle your data",
                    onClick = {}
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Info Section
            SettingsSection(title = "About") {
                SettingsClickableItem(
                    icon = Icons.Default.Info,
                    title = "App Version",
                    subtitle = "1.0.0 (Build 124)",
                    onClick = {}
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                letterSpacing = 1.sp
            ),
            modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
        ) {
            content()
        }
    }
}

@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsIconBox(icon)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = DarkNavy
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Primary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.LightGray.copy(alpha = 0.5f),
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

@Composable
fun SettingsClickableItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsIconBox(icon)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = DarkNavy
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun SettingsIconBox(icon: ImageVector) {
    Surface(
        modifier = Modifier.size(40.dp),
        shape = RoundedCornerShape(12.dp),
        color = Background
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

