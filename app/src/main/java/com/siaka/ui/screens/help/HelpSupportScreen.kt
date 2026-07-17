package com.siaka.ui.screens.help

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
fun HelpSupportScreen(
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Help & Support",
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
            Text(
                text = "How Siaka Works",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = PrimaryDark
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Step-by-Step Guide
            StepItem(
                number = "1",
                title = "Set Your Destination",
                description = "Open the Map tab and use the search bar or tap on the map to choose where you want to go."
            )
            StepItem(
                number = "2",
                title = "Select Your Route",
                description = "Choose from recommended bicycle-friendly routes. We prioritize safety and scenic paths."
            )
            StepItem(
                number = "3",
                title = "Start Your Journey",
                description = "Tap 'Start Ride' to begin turn-by-turn navigation. Keep your eyes on the road and stay safe!"
            )
            StepItem(
                number = "4",
                title = "Review Your History",
                description = "After your ride, check your 'Ride History' in the Profile tab to see your progress and past routes."
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Guidelines Section
            Text(
                text = "Safety Guidelines",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = DarkNavy
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            GuidelineCard(
                icon = Icons.Default.DirectionsBike,
                title = "Helmet Always",
                description = "Protect your head. Always wear a properly fitted helmet while riding."
            )
            GuidelineCard(
                icon = Icons.Default.Visibility,
                title = "Stay Visible",
                description = "Use lights at night and wear reflective clothing when possible."
            )
            GuidelineCard(
                icon = Icons.Default.Traffic,
                title = "Obey Traffic Laws",
                description = "Cyclists are subject to the same rules as drivers. Stop at red lights and signs."
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun StepItem(
    number: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = RoundedCornerShape(8.dp),
            color = SoftGreen
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = number,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = DarkNavy
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun GuidelineCard(
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = DarkNavy
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}
