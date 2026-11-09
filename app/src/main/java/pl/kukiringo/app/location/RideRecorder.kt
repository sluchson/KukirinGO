package pl.kukiringo.app.location

import android.location.Location
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import pl.kukiringo.app.data.model.Ride
import pl.kukiringo.app.data.model.TrackPoint
import pl.kukiringo.app.data.repository.KukirinRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class RideUiState(
    val isRecording: Boolean = false,
    val elapsedSec: Int = 0,
    val distanceKm: Double = 0.0,
    val speedKmh: Double = 0.0,
    val maxSpeedKmh: Double = 0.0,
    val path: List<TrackPoint> = emptyList(),
    val current: TrackPoint? = null,
    val hasGpsFix: Boolean = false,
    val accuracyM: Float? = null,
    val lastSavedTitle: String? = null,
)

/**
 * Nagrywanie przejazdu z prawdziwego GPS. Żyje poza ekranem, więc nagrywanie
 * trwa także po przejściu na inną zakładkę.
 */
class RideRecorder(
    private val tracker: LocationTracker,
    private val repository: KukirinRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _state = MutableStateFlow(RideUiState())
    val state: StateFlow<RideUiState> = _state.asStateFlow()

    private var timerJob: Job? = null
    private var locationJob: Job? = null
    private var startedAt = 0L
    private var lastPoint: Location? = null

    fun toggle() {
        if (_state.value.isRecording) stop() else start()
    }

    fun start() {
        if (_state.value.isRecording) return
        startedAt = System.currentTimeMillis()
        lastPoint = null
        _state.value = RideUiState(isRecording = true)
        tracker.acquire()

        timerJob = scope.launch {
            while (isActive) {
                delay(1_000)
                _state.update { it.copy(elapsedSec = ((System.currentTimeMillis() - startedAt) / 1000).toInt()) }
            }
        }
        locationJob = scope.launch {
            tracker.location.collect { loc -> if (loc != null && loc.time >= startedAt - 5_000) onLocation(loc) }
        }
    }

    fun stop() {
        if (!_state.value.isRecording) return
        timerJob?.cancel()
        locationJob?.cancel()
        tracker.release()

        val s = _state.value
        val elapsedSec = ((System.currentTimeMillis() - startedAt) / 1000).toInt()
        var savedTitle: String? = null
        if (s.distanceKm >= 0.01 || elapsedSec >= 10) {
            val hours = elapsedSec / 3600.0
            val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(startedAt))
            savedTitle = "Przejazd z GPS"
            repository.addRide(
                Ride(
                    id = "gps_$startedAt",
                    title = savedTitle,
                    dateLabel = "Dziś · $time",
                    distanceKm = s.distanceKm,
                    durationMin = (elapsedSec / 60).coerceAtLeast(1),
                    avgSpeedKmh = if (hours > 0) s.distanceKm / hours else 0.0,
                    batteryUsedPct = null,
                    props = 0,
                    path = s.path,
                )
            )
        }
        _state.update { it.copy(isRecording = false, speedKmh = 0.0, elapsedSec = elapsedSec, lastSavedTitle = savedTitle) }
    }

    private fun onLocation(loc: Location) {
        val point = TrackPoint(loc.latitude, loc.longitude)
        val speed = if (loc.hasSpeed()) loc.speed * 3.6 else 0.0
        _state.update { it.copy(current = point, hasGpsFix = true, accuracyM = loc.accuracy, speedKmh = speed) }

        // Do dystansu tylko dokładne pozycje, a drobne „drgania” GPS pomijamy.
        if (loc.accuracy > 35f) return
        val prev = lastPoint
        if (prev == null) {
            lastPoint = loc
            _state.update { it.copy(path = listOf(point)) }
            return
        }
        val meters = prev.distanceTo(loc)
        if (meters < 4f) return
        lastPoint = loc
        _state.update {
            it.copy(
                distanceKm = it.distanceKm + meters / 1000.0,
                path = it.path + point,
                maxSpeedKmh = maxOf(it.maxSpeedKmh, speed),
            )
        }
    }
}
