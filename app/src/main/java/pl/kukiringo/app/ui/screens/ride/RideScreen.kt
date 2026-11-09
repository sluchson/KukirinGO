package pl.kukiringo.app.ui.screens.ride

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import pl.kukiringo.app.data.model.Ride
import pl.kukiringo.app.data.repository.AppContainer
import pl.kukiringo.app.ui.components.KukiCard
import pl.kukiringo.app.ui.components.OsmMap
import pl.kukiringo.app.ui.components.ScreenHeader
import pl.kukiringo.app.ui.components.StatTile
import pl.kukiringo.app.ui.components.TrackLocationWhileVisible
import pl.kukiringo.app.ui.components.rememberLocationPermission

@Composable
fun RideScreen() {
    val recorder = AppContainer.rideRecorder
    val state by recorder.state.collectAsState()
    val rides by AppContainer.repository.rides.collectAsState()
    val location by AppContainer.locationTracker.location.collectAsState()
    val permission = rememberLocationPermission(onGranted = { recorder.start() })

    // GPS „rozgrzewa się” już na ekranie, żeby start był szybszy.
    TrackLocationWhileVisible()

    // Ekran nie gaśnie podczas nagrywania.
    val view = LocalView.current
    DisposableEffect(state.isRecording) {
        view.keepScreenOn = state.isRecording
        onDispose { view.keepScreenOn = false }
    }

    val me = state.current ?: location?.let { pl.kukiringo.app.data.model.TrackPoint(it.latitude, it.longitude) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { ScreenHeader(eyebrow = "Jazda", title = if (state.isRecording) "Nagrywam przejazd" else "Nowy przejazd") }
        item {
            OsmMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(24.dp)),
                path = state.path,
                me = me,
                followMe = true,
                zoom = 16.0,
            )
        }
        item {
            Text(
                text = formatTime(state.elapsedSec),
                style = MaterialTheme.typography.displaySmall,
                color = if (state.isRecording) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(
                    value = "%.2f km".format(state.distanceKm),
                    label = "dystans",
                    modifier = Modifier.weight(1f),
                    highlight = state.isRecording,
                )
                StatTile(
                    value = "%.0f km/h".format(state.speedKmh),
                    label = "prędkość",
                    modifier = Modifier.weight(1f),
                )
                StatTile(
                    value = "%.0f km/h".format(state.maxSpeedKmh),
                    label = "maks.",
                    modifier = Modifier.weight(1f),
                )
            }
        }
        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(
                    onClick = { if (permission.granted || state.isRecording) recorder.toggle() else permission.request() },
                    shape = CircleShape,
                    modifier = Modifier.size(112.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Icon(
                        imageVector = if (state.isRecording) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                        contentDescription = if (state.isRecording) "Zakończ przejazd" else "Start",
                        modifier = Modifier.size(52.dp),
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = statusText(permission.granted, state.isRecording, state.hasGpsFix, state.accuracyM, state.lastSavedTitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item { Text("Ostatnie przejazdy", style = MaterialTheme.typography.titleLarge) }
        items(rides, key = { it.id }) { ride -> RideRow(ride) }
    }
}

private fun statusText(granted: Boolean, recording: Boolean, fix: Boolean, accuracy: Float?, saved: String?): String = when {
    !granted -> "Kliknij start i zezwól na lokalizację"
    recording && !fix -> "Szukam sygnału GPS… wyjdź na zewnątrz"
    recording -> "GPS ±${accuracy?.toInt() ?: "?"} m · kliknij ■, żeby zakończyć"
    saved != null -> "Zapisano przejazd — jest na liście poniżej"
    else -> "Kliknij, żeby ruszyć"
}

@Composable
private fun RideRow(ride: Ride) {
    KukiCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(ride.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        ride.dateLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(
                    Icons.Filled.ThumbUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text("${ride.props}", style = MaterialTheme.typography.labelLarge)
            }
            val battery = ride.batteryUsedPct?.let { " · bateria −$it%" } ?: ""
            Text(
                text = "%.2f km · %d min · %.0f km/h".format(ride.distanceKm, ride.durationMin, ride.avgSpeedKmh) + battery,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

private fun formatTime(totalSec: Int): String {
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return "%d:%02d:%02d".format(h, m, s)
}
