package pl.kukiringo.app.ui.screens.ride

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class RideUiState(
    val isRecording: Boolean = false,
    val elapsedSec: Int = 0,
    val distanceKm: Double = 0.0,
    val speedKmh: Double = 0.0,
)

/**
 * Nagrywanie przejazdu. Na razie symuluje prędkość co sekundę.
 * Zadanie dla zespołu: podmienić symulację na prawdziwy GPS
 * (FusedLocationProviderClient + foreground service).
 */
class RideViewModel : ViewModel() {

    private val _state = MutableStateFlow(RideUiState())
    val state: StateFlow<RideUiState> = _state.asStateFlow()

    private var job: Job? = null

    fun toggleRecording() {
        if (_state.value.isRecording) stop() else start()
    }

    private fun start() {
        _state.value = RideUiState(isRecording = true)
        job = viewModelScope.launch {
            while (isActive) {
                delay(1_000)
                _state.update { s ->
                    val seconds = s.elapsedSec + 1
                    val speed = 15.0 + (seconds % 7) // dane demo
                    s.copy(
                        elapsedSec = seconds,
                        speedKmh = speed,
                        distanceKm = s.distanceKm + speed / 3600.0,
                    )
                }
            }
        }
    }

    private fun stop() {
        job?.cancel()
        job = null
        _state.update { it.copy(isRecording = false, speedKmh = 0.0) }
    }
}
