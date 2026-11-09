package pl.kukiringo.app.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Pozycja z GPS (i sieci, gdy GPS nie łapie, np. w budynku).
 * Ekrany wołają [acquire] / [release] — GPS działa tylko, gdy ktoś go potrzebuje.
 */
class LocationTracker(private val context: Context) {

    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val _location = MutableStateFlow<Location?>(null)
    val location: StateFlow<Location?> = _location.asStateFlow()

    private var users = 0
    private var running = false

    private val listener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            val current = _location.value
            if (current == null || isBetter(location, current)) _location.value = location
        }

        // Starsze Androidy (8–10) wymagają nadpisania wszystkich metod.
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}

        @Deprecated("Wymagane na Androidzie < 10")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    }

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    fun acquire() {
        users++
        start()
    }

    fun release() {
        users = (users - 1).coerceAtLeast(0)
        if (users == 0) stop()
    }

    /** Ponowna próba, np. zaraz po przyznaniu zgody na lokalizację. */
    fun refresh() {
        if (users > 0) start()
    }

    @SuppressLint("MissingPermission")
    private fun start() {
        if (running || !hasPermission()) return
        running = true
        for (provider in listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)) {
            if (!locationManager.allProviders.contains(provider)) continue
            try {
                locationManager.requestLocationUpdates(provider, 1000L, 0f, listener, Looper.getMainLooper())
                locationManager.getLastKnownLocation(provider)?.let { listener.onLocationChanged(it) }
            } catch (e: Exception) {
                // dostawca wyłączony albo niedostępny — próbujemy kolejnego
            }
        }
    }

    private fun stop() {
        if (!running) return
        running = false
        try {
            locationManager.removeUpdates(listener)
        } catch (e: Exception) {
        }
    }

    /** Nowsza pozycja wygrywa, chyba że jest dużo mniej dokładna niż świeża poprzednia. */
    private fun isBetter(new: Location, current: Location): Boolean {
        val ageDiff = new.time - current.time
        if (ageDiff > 10_000) return true
        if (ageDiff < -10_000) return false
        return new.accuracy <= current.accuracy + 15f
    }
}
