package com.siaka.ui.screens.profile

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.siaka.R
import com.siaka.data.local.CompletedRide
import com.siaka.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideHistoryScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val rideHistory by viewModel.rideHistory.collectAsStateWithLifecycle()
    val totalDistance by viewModel.totalDistance.collectAsStateWithLifecycle()
    val rideCount by viewModel.rideCount.collectAsStateWithLifecycle()

    var showContents by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        showContents = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ride History", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Summary Stats Card
            item {
                AnimatedVisibility(
                    visible = showContents,
                    enter = slideInVertically(
                        initialOffsetY = { 40 },
                        animationSpec = tween(500)
                    ) + fadeIn(animationSpec = tween(500))
                ) {
                    RideSummaryStats(
                        totalKm = totalDistance ?: 0.0,
                        totalRides = rideCount
                    )
                }
            }

            item {
                AnimatedVisibility(
                    visible = showContents,
                    enter = slideInVertically(
                        initialOffsetY = { 40 },
                        animationSpec = tween(500, delayMillis = 100)
                    ) + fadeIn(animationSpec = tween(500, delayMillis = 100))
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Past Journeys",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDark
                        )
                    }
                }
            }

            if (rideHistory.isEmpty()) {
                item {
                    EmptyHistoryState()
                }
            } else {
                itemsIndexed(rideHistory) { index, ride ->
                    var itemVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        delay(200 + index * 100L)
                        itemVisible = true
                    }
                    AnimatedVisibility(
                        visible = itemVisible,
                        enter = slideInVertically(
                            initialOffsetY = { 50 },
                            animationSpec = tween(500)
                        ) + fadeIn(animationSpec = tween(500))
                    ) {
                        HistoryRideCard(ride)
                    }
                }
            }
        }
    }
}

@Composable
fun RideSummaryStats(totalKm: Double, totalRides: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryDark)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color.White.copy(alpha = 0.7f))
                Text(
                    text = String.format(Locale.getDefault(), "%.1f km", totalKm),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text("Total Distance", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.7f))
            }
            
            Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color.White.copy(alpha = 0.2f)).align(Alignment.CenterVertically))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.History, contentDescription = null, tint = Color.White.copy(alpha = 0.7f))
                Text(
                    text = totalRides.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text("Total Rides", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun HistoryRideCard(ride: CompletedRide) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = SoftGreen.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(painterResource(R.drawable.route_filled), contentDescription = null, tint = Secondary, modifier = Modifier.size(24.dp))
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                val date = remember(ride.timestamp) {
                    SimpleDateFormat("EEEE, MMM dd", Locale.getDefault()).format(Date(ride.timestamp))
                }
                Text(date, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                    Text(
                        text = " ${formatDuration(ride.durationSeconds)} • ${String.format(Locale.getDefault(), "%.1f", ride.distanceKm)} km",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format(Locale.getDefault(), "%.1f", ride.avgSpeedKmh),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Primary
                )
                Text("km/h", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}

@Composable
fun EmptyHistoryState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))
        Text("No rides recorded yet.", color = Color.Gray)
    }
}

private fun formatDuration(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}
