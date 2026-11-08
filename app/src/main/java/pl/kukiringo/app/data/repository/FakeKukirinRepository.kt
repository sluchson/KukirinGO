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
            Ride("r1", "Dojazd na wykład", "Dziś · 7:42", 6.2, 21, 17.7, 18, 24),
            Ride("r2", "Bulwary po zajęciach", "Wczoraj · 16:10", 8.7, 29, 17.9, 22, 11),
            Ride("r3", "Nocny objazd Rzeszowa", "Sob · 21:05", 15.2, 52, 17.5, 41, 37),
        )
    )
    override val rides: StateFlow<List<Ride>> = _rides.asStateFlow()

    private val _kukistops = MutableStateFlow(
        listOf(
            Kukistop("k1", "Wydział EiI PRz", "Zalicz po wykładzie", 0.0, true),
            Kukistop("k2", "Politechnika Rzeszowska", "Kampus przy al. Powstańców Warszawy", 0.9, true),
            Kukistop("k3", "Rzeszowskie Bulwary", "Najlepsza nawierzchnia nad Wisłokiem", 1.2, true),
            Kukistop("k4", "Zamek Lubomirskich", "Objedź zamek dookoła", 1.3, false),
            Kukistop("k5", "Zalew Wisłok", "Długa prosta wzdłuż wody", 1.7, false),
            Kukistop("k6", "Pomnik Czynu Rewolucyjnego", "Najbardziej rozpoznawalny punkt w mieście", 1.8, false),
            Kukistop("k7", "Studnia na Rynku", "Serce Rzeszowa — zbiórka większości zlotów", 1.8, false),
        )
    )
    override val kukistops: StateFlow<List<Kukistop>> = _kukistops.asStateFlow()

    private val _meetups = MutableStateFlow(
        listOf(
            Meetup("m1", "Nocny przejazd bulwarami", "Sobota, 19:00", "Studnia na Rynku", 18.0, "spokojne tempo", 24, 40, false),
            Meetup("m2", "Kukistopowy maraton po Rzeszowie", "Niedziela, 11:00", "Wydział EiI PRz", 25.0, "średnie tempo", 12, 20, true),
            Meetup("m3", "Pierwszy zlot dla nowych", "Środa, 17:30", "Zalew Wisłok", 8.0, "na luzie", 6, 15, false),
        )
    )
    override val meetups: StateFlow<List<Meetup>> = _meetups.asStateFlow()

    private val _clips = MutableStateFlow(
        listOf(
            Clip("c1", "@kuki_ania", "Zachód słońca na bulwarach 🔥", "Rzeszowskie Bulwary", 128, 14, false),
            Clip("c2", "@wiktor.rides", "Nowy rekord kukistopów w jeden wieczór", "Rynek, Rzeszów", 342, 41, true),
            Clip("c3", "@mateusz_go", "Zlot nr 1 — było nas 24!", "Zalew Wisłok", 87, 9, false),
        )
    )
    override val clips: StateFlow<List<Clip>> = _clips.asStateFlow()

    private val _profile = MutableStateFlow(
        UserProfile(
            nick = "kukirider",
            totalKm = 236.4,
            ridesCount = 31,
            kukistopsVisited = 3,
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
