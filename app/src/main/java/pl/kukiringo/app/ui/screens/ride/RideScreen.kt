package pl.kukiringo.app.ui.screens.ride

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pl.kukiringo.app.data.model.Ride
import pl.kukiringo.app.data.repository.AppContainer
import pl.kukiringo.app.ui.components.KukiCard
import pl.kukiringo.app.ui.components.ScreenHeader
import pl.kukiringo.app.ui.components.StatTile

@Composable
fun RideScreen(viewModel: RideViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    val rides by AppContainer.repository.rides.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { ScreenHeader(eyebrow = "Jazda", title = "Nowy przejazd") }
        item {
            Text(
                text = formatTime(state.elapsedSec),
                style = MaterialTheme.typography.displaySmall,
                color = if (state.isRecording) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
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
            }
        }
        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(
                    onClick = viewModel::toggleRecording,
                    shape = CircleShape,
                    modifier = Modifier.size(128.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Icon(
                        imageVector = if (state.isRecording) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                        contentDescription = if (state.isRecording) "Zakończ przejazd" else "Start",
                        modifier = Modifier.size(56.dp),
                    )
                }
                Spacer(Modifier.padding(4.dp))
                Text(
                    text = if (state.isRecording) "Nagrywam… (dane demo)" else "Kliknij, żeby ruszyć",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item { Text("Ostatnie przejazdy", style = MaterialTheme.typography.titleLarge) }
        items(rides, key = { it.id }) { ride -> RideRow(ride) }
    }
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
            Text(
                text = "%.1f km · %d min · %.0f km/h · bateria −%d%%".format(
                    ride.distanceKm, ride.durationMin, ride.avgSpeedKmh, ride.batteryUsedPct,
                ),
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
