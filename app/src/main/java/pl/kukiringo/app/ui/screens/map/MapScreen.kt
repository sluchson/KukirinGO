package pl.kukiringo.app.ui.screens.map

import android.location.Location
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import pl.kukiringo.app.data.model.Kukistop
import pl.kukiringo.app.data.model.TrackPoint
import pl.kukiringo.app.data.repository.AppContainer
import pl.kukiringo.app.ui.components.KukiCard
import pl.kukiringo.app.ui.components.MapPin
import pl.kukiringo.app.ui.components.OsmMap
import pl.kukiringo.app.ui.components.ScreenHeader
import pl.kukiringo.app.ui.components.TrackLocationWhileVisible
import pl.kukiringo.app.ui.components.rememberLocationPermission

@Composable
fun MapScreen() {
    val repository = AppContainer.repository
    val kukistops by repository.kukistops.collectAsState()
    val location by AppContainer.locationTracker.location.collectAsState()
    val permission = rememberLocationPermission()

    TrackLocationWhileVisible()

    val me = location?.let { TrackPoint(it.latitude, it.longitude) }
    // Najbliższe kukistopy na górze listy (gdy znamy pozycję).
    val sorted = if (me == null) kukistops else kukistops.sortedBy { distanceM(me, it) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { ScreenHeader(eyebrow = "Mapa", title = "Kukistopy w Rzeszowie") }
        item {
            OsmMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .clip(RoundedCornerShape(24.dp)),
                pins = kukistops.map {
                    MapPin(it.id, it.lat, it.lng, it.name, it.description, done = it.visited)
                },
                me = me,
                zoom = 13.5,
            )
        }
        item {
            if (!permission.granted) {
                OutlinedButton(onClick = permission.request, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.MyLocation, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Pokaż moją pozycję")
                }
            } else {
                Text(
                    text = if (location == null) "Szukam sygnału GPS…"
                    else "Twoja pozycja: dokładność ±${location!!.accuracy.toInt()} m",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        items(sorted, key = { it.id }) { stop ->
            KukistopRow(
                stop = stop,
                distanceM = me?.let { distanceM(it, stop) },
                onVisit = { repository.visitKukistop(stop.id) },
            )
        }
    }
}

private fun distanceM(me: TrackPoint, stop: Kukistop): Float {
    val result = FloatArray(1)
    Location.distanceBetween(me.lat, me.lng, stop.lat, stop.lng, result)
    return result[0]
}

private fun formatDistance(meters: Float): String =
    if (meters < 1000) "${meters.toInt()} m" else "%.1f km".format(meters / 1000)

@Composable
private fun KukistopRow(stop: Kukistop, distanceM: Float?, onVisit: () -> Unit) {
    val inRange = distanceM != null && distanceM <= stop.radiusM
    KukiCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = if (stop.visited) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp),
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(stop.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        stop.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = distanceM?.let { formatDistance(it) } ?: "—",
                        style = MaterialTheme.typography.labelLarge,
                    )
                    if (stop.visited) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "zaliczony",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
            if (!stop.visited && inRange) {
                Button(onClick = onVisit, modifier = Modifier.fillMaxWidth()) {
                    Text("Jesteś na miejscu — zalicz kukistop!")
                }
            }
        }
    }
}
