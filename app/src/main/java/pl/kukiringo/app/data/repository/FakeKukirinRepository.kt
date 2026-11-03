package pl.kukiringo.app.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import pl.kukiringo.app.data.model.Badge
import pl.kukiringo.app.data.model.Clip
import pl.kukiringo.app.data.model.Kukistop
import pl.kukiringo.app.data.model.Meetup
import pl.kukiringo.app.data.model.Ride
import pl.kukiringo.app.data.model.UserProfile

/** Dane przykładowe, żeby frontend mógł pracować, zanim backend będzie gotowy. */
class FakeKukirinRepository : KukirinRepository {

    private val _rides = MutableStateFlow(
        listOf(
            Ride("r1", "Poranny dojazd", "Dziś · 7:42", 12.4, 41, 18.1, 34, 24),
            Ride("r2", "Bulwary po zajęciach", "Wczoraj · 16:10", 8.7, 29, 17.9, 22, 11),
            Ride("r3", "Nocny objazd rynku", "Sob · 21:05", 15.2, 52, 17.5, 41, 37),
        )
    )
    override val rides: StateFlow<List<Ride>> = _rides.asStateFlow()

    private val _kukistops = MutableStateFlow(
        listOf(
            Kukistop("k1", "Rynek Główny", "Serce miasta, zbiórka większości zlotów", 0.8, true),
            Kukistop("k2", "Bulwary nad rzeką", "Najlepsza nawierzchnia w okolicy", 1.6, true),
            Kukistop("k3", "Kampus uczelni", "Zalicz po wykładzie", 2.3, false),
            Kukistop("k4", "Punkt widokowy", "Wymagający podjazd — bateria w górę!", 4.9, false),
        )
    )
    override val kukistops: StateFlow<List<Kukistop>> = _kukistops.asStateFlow()

    private val _meetups = MutableStateFlow(
        listOf(
            Meetup("m1", "Nocny przejazd bulwarami", "Sobota, 19:00", "Rynek", 18.0, "spokojne tempo", 24, 40, false),
            Meetup("m2", "Kukistopowy maraton", "Niedziela, 11:00", "Kampus", 25.0, "średnie tempo", 12, 20, true),
            Meetup("m3", "Pierwszy zlot dla nowych", "Środa, 17:30", "Park miejski", 8.0, "na luzie", 6, 15, false),
        )
    )
    override val meetups: StateFlow<List<Meetup>> = _meetups.asStateFlow()

    private val _clips = MutableStateFlow(
        listOf(
            Clip("c1", "@kuki_ania", "Zachód słońca na bulwarach 🔥", "Bulwary", 128, 14, false),
            Clip("c2", "@wiktor.rides", "Nowy rekord kukistopów w jeden wieczór", "Rynek", 342, 41, true),
            Clip("c3", "@mateusz_go", "Zlot nr 1 — było nas 24!", "Park miejski", 87, 9, false),
        )
    )
    override val clips: StateFlow<List<Clip>> = _clips.asStateFlow()

    private val _profile = MutableStateFlow(
        UserProfile(
            nick = "kukirider",
            totalKm = 236.4,
            ridesCount = 31,
            kukistopsVisited = 18,
            badges = listOf(
                Badge("Pierwsze 100 km", "Przejedź łącznie 100 km", true),
                Badge("Nocny marek", "Przejazd po 22:00", true),
                Badge("Zlotowicz", "Weź udział w 5 zlotach", false),
                Badge("Kolekcjoner", "Zalicz 50 kukistopów", false),
            ),
        )
    )
    override val profile: StateFlow<UserProfile> = _profile.asStateFlow()

    override fun toggleJoinMeetup(meetupId: String) {
        _meetups.update { list ->
            list.map { m ->
                if (m.id != meetupId) {
                    m
                } else if (m.joined) {
                    m.copy(joined = false, participants = m.participants - 1)
                } else if (m.participants < m.limit) {
                    m.copy(joined = true, participants = m.participants + 1)
                } else {
                    m
                }
            }
        }
    }

    override fun toggleLikeClip(clipId: String) {
        _clips.update { list ->
            list.map { c ->
                if (c.id != clipId) c
                else c.copy(likedByMe = !c.likedByMe, likes = c.likes + if (c.likedByMe) -1 else 1)
            }
        }
    }
}
