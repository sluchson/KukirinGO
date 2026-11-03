package pl.kukiringo.app.data.repository

import kotlinx.coroutines.flow.StateFlow
import pl.kukiringo.app.data.model.Clip
import pl.kukiringo.app.data.model.Kukistop
import pl.kukiringo.app.data.model.Meetup
import pl.kukiringo.app.data.model.Ride
import pl.kukiringo.app.data.model.UserProfile

/**
 * Jedyne miejsce, przez które ekrany pobierają dane.
 *
 * Teraz działa na danych przykładowych ([FakeKukirinRepository]).
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
}

/** Prosty „kontener zależności”. Później można go zamienić na Hilt. */
object AppContainer {
    val repository: KukirinRepository by lazy { FakeKukirinRepository() }
}
