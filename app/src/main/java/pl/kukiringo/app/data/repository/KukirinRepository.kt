package pl.kukiringo.app.data.repository

import android.content.Context
import kotlinx.coroutines.flow.StateFlow
import pl.kukiringo.app.data.model.Clip
import pl.kukiringo.app.data.model.Kukistop
import pl.kukiringo.app.data.model.Meetup
import pl.kukiringo.app.data.model.Ride
import pl.kukiringo.app.data.model.UserProfile
import pl.kukiringo.app.location.LocationTracker
import pl.kukiringo.app.location.RideRecorder
import java.io.File

/**
 * Jedyne miejsce, przez które ekrany pobierają dane.
 *
 * Teraz działa lokalnie ([FakeKukirinRepository]): dane przykładowe z Rzeszowa
 * plus prawdziwe przejazdy z GPS i klipy zapisane na telefonie.
 * Zespół backendu dopisze `SupabaseKukirinRepository` z tym samym interfejsem
 * i podmieni go w [AppContainer] — ekrany nie będą wymagały zmian.
 */
interface KukirinRepository {
    val rides: StateFlow<List<Ride>>
    val kukistops: StateFlow<List<Kukistop>>
    val meetups: StateFlow<List<Meetup>>
    val clips: StateFlow<List<Clip>>
    val profile: StateFlow<UserProfile>

    fun toggleJoinMeetup(meetupId: String)
    fun toggleLikeClip(clipId: String)
    fun addRide(ride: Ride)
    fun addClip(videoFile: File)
    fun visitKukistop(kukistopId: String)
}

/** Prosty „kontener zależności”. Inicjowany w [pl.kukiringo.app.KukirinApplication]. */
object AppContainer {
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    /** Folder z klipami kukiTok w pamięci aplikacji. */
    val clipsDir: File? get() = if (::appContext.isInitialized) File(appContext.filesDir, "clips") else null

    val repository: KukirinRepository by lazy { FakeKukirinRepository(clipsDir) }
    val locationTracker: LocationTracker by lazy { LocationTracker(appContext) }
    val rideRecorder: RideRecorder by lazy { RideRecorder(locationTracker, repository) }
}
