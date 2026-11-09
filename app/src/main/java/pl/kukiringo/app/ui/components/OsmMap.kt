package pl.kukiringo.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import pl.kukiringo.app.R
import pl.kukiringo.app.data.model.TrackPoint

/** Centrum Rzeszowa — widok startowy mapy. */
val RZESZOW_CENTER = TrackPoint(50.0310, 21.9990)

data class MapPin(
    val id: String,
    val lat: Double,
    val lng: Double,
    val title: String,
    val snippet: String,
    val done: Boolean,
)

/**
 * Mapa OpenStreetMap (osmdroid) w Compose: pinezki, trasa i pozycja użytkownika.
 * [followMe] = mapa przesuwa się za użytkownikiem (podczas jazdy).
 */
@Composable
fun OsmMap(
    modifier: Modifier = Modifier,
    pins: List<MapPin> = emptyList(),
    path: List<TrackPoint> = emptyList(),
    me: TrackPoint? = null,
    followMe: Boolean = false,
    zoom: Double = 14.0,
    center: TrackPoint = RZESZOW_CENTER,
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            controller.setZoom(zoom)
            controller.setCenter(GeoPoint(center.lat, center.lng))
        }
    }

    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onPause()
            mapView.onDetach()
        }
    }

    val orange = ContextCompat.getColor(context, R.color.kukirin_orange)
    val grey = 0xFF8F8781.toInt()

    AndroidView(
        modifier = modifier,
        factory = { mapView },
        update = { map ->
            map.overlays.clear()

            if (path.size >= 2) {
                map.overlays.add(
                    Polyline(map).apply {
                        setPoints(path.map { GeoPoint(it.lat, it.lng) })
                        outlinePaint.color = orange
                        outlinePaint.strokeWidth = 14f
                    }
                )
            }

            pins.forEach { pin ->
                map.overlays.add(
                    Marker(map).apply {
                        position = GeoPoint(pin.lat, pin.lng)
                        title = pin.title
                        snippet = pin.snippet
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        setIcon(
                            ContextCompat.getDrawable(context, R.drawable.ic_kukistop)?.mutate()?.apply {
                                setTint(if (pin.done) grey else orange)
                            }
                        )
                    }
                )
            }

            me?.let { point ->
                map.overlays.add(
                    Marker(map).apply {
                        position = GeoPoint(point.lat, point.lng)
                        title = "Tu jesteś"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        setIcon(ContextCompat.getDrawable(context, R.drawable.ic_me))
                    }
                )
                if (followMe) map.controller.animateTo(GeoPoint(point.lat, point.lng))
            }

            map.invalidate()
        },
    )
}
