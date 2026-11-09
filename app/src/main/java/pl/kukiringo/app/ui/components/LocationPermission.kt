package pl.kukiringo.app.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import pl.kukiringo.app.data.repository.AppContainer

class LocationPermissionState(val granted: Boolean, val request: () -> Unit)

/** Stan zgody na lokalizację + funkcja, która o nią prosi. */
@Composable
fun rememberLocationPermission(onGranted: () -> Unit = {}): LocationPermissionState {
    val tracker = AppContainer.locationTracker
    var granted by remember { mutableStateOf(tracker.hasPermission()) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        granted = result.values.any { it }
        if (granted) {
            tracker.refresh()
            onGranted()
        }
    }
    return LocationPermissionState(granted) {
        launcher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }
}

/** Włącza GPS, dopóki ekran jest widoczny. */
@Composable
fun TrackLocationWhileVisible() {
    val tracker = AppContainer.locationTracker
    DisposableEffect(Unit) {
        tracker.acquire()
        onDispose { tracker.release() }
    }
}
