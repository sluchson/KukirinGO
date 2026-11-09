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
import java.io.File

/**
 * Dane lokalne: przykłady z Rzeszowa + prawdziwe przejazdy i klipy z telefonu.
 * [clipsDir] = folder z nagranymi/dodanymi klipami (null w testach).
 */
class FakeKukirinRepository(private val clipsDir: File? = null) : KukirinRepository {

    private val _rides = MutableStateFlow(
        listOf(
            Ride("r1", "Dojazd na wykład", "Dziś · 7:42", 6.2, 21, 17.7, 18, 24),
            Ride("r2", "Bulwary po zajęciach", "Wczoraj · 16:10", 8.7, 29, 17.9, 22, 11),
            Ride("r3", "Nocny objazd Rzeszowa", "Sob · 21:05", 15.2, 52, 17.5, 41, 37),
        )
    )
    override val rides: StateFlow<List<Ride>> = _rides.asStateFlow()

    // Współrzędne z Google Maps — te same co w supabase/seed.sql.
    private val _kukistops = MutableStateFlow(
        listOf(
            Kukistop("k1", "Wydział EiI PRz", "Zalicz po wykładzie", 50.0270339, 21.9851869, 120, false),
            Kukistop("k2", "Politechnika Rzeszowska", "Kampus przy al. Powstańców Warszawy", 50.019034, 21.989195, 150, false),
            Kukistop("k3", "Rzeszowskie Bulwary", "Najlepsza nawierzchnia nad Wisłokiem", 50.0262763, 22.0020559, 150, false),
            Kukistop("k4", "Zamek Lubomirskich", "Objedź zamek dookoła", 50.0324847, 22.0006347, 80, false),
            Kukistop("k5", "Zalew Wisłok", "Długa prosta wzdłuż wody", 50.0135542, 21.9951959, 300, false),
            Kukistop("k6", "Pomnik Czynu Rewolucyjnego", "Najbardziej rozpoznawalny punkt w mieście", 50.0406072, 21.9994961, 60, false),
            Kukistop("k7", "Studnia na Rynku", "Serce Rzeszowa — zbiórka większości zlotów", 50.0375035, 22.0049323, 60, false),
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

    private val sampleClips = listOf(
        Clip("c1", "@kuki_ania", "Zachód słońca na bulwarach 🔥", "Rzeszowskie Bulwary", 128, 14, false),
        Clip("c2", "@wiktor.rides", "Nowy rekord kukistopów w jeden wieczór", "Rynek, Rzeszów", 342, 41, true),
        Clip("c3", "@mateusz_go", "Zlot nr 1 — było nas 24!", "Zalew Wisłok", 87, 9, false),
    )

    // Musi być przed _clips — używane przy wczytywaniu klipów.
    private val demoInfo = listOf(
        Triple("@kuki_ania", "Wieczorna przejażdżka 🛴🔥", "Rzeszowskie Bulwary"),
        Triple("@wiktor.rides", "Kukistop zaliczony ✅", "Rynek, Rzeszów"),
        Triple("@mateusz_go", "Ekipa w trasie — zlot nr 1", "Zalew Wisłok"),
    )

    private val _clips = MutableStateFlow(loadClips())
    override val clips: StateFlow<List<Clip>> = _clips.asStateFlow()

    private val _profile = MutableStateFlow(
        UserProfile(
            nick = "kukirider",
            totalKm = 236.4,
            ridesCount = 31,
            kukistopsVisited = 0,
            badges = listOf(
                Badge("Pierwsze 100 km", "Przejedź łącznie 100 km", true),
                Badge("Nocny marek", "Przejazd po 22:00", true),
                Badge("Zlotowicz", "Weź udział w 5 zlotach", false),
                Badge("Kolekcjoner", "Zalicz 50 kukistopów", false),
            ),
        )
    )
    override val profile: StateFlow<UserProfile> = _profile.asStateFlow()

    /** Klipy z telefonu (najnowsze pierwsze); gdy nie ma żadnego — przykładowe. */
    private fun loadClips(): List<Clip> {
        val files = clipsDir?.listFiles { f -> f.isFile && f.length() > 0 }
            ?.sortedByDescending { it.lastModified() }
            .orEmpty()
        return if (files.isEmpty()) sampleClips else files.map(::clipFromFile)
    }

    private fun clipFromFile(file: File): Clip {
        // demo_1.mp4, demo_2.mp4… — klipy do pokazu; reszta to klipy nagrane w aplikacji.
        val demoIndex = file.nameWithoutExtension.removePrefix("demo_").toIntOrNull()
        val demo = if (file.name.startsWith("demo_") && demoIndex != null) {
            demoInfo[(demoIndex - 1).mod(demoInfo.size)]
        } else {
            null
        }
        return Clip(
            id = file.name,
            author = demo?.first ?: "@kukirider",
            caption = demo?.second ?: "Mój przejazd 🛴",
            location = demo?.third ?: "Rzeszów",
            likes = if (demo != null) 87 + 41 * (demoIndex ?: 0) else 0,
            comments = if (demo != null) 3 + 4 * (demoIndex ?: 0) else 0,
            likedByMe = false,
            videoPath = file.absolutePath,
        )
    }

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

    override fun addRide(ride: Ride) {
        _rides.update { listOf(ride) + it }
        _profile.update { it.copy(totalKm = it.totalKm + ride.distanceKm, ridesCount = it.ridesCount + 1) }
    }

    override fun addClip(videoFile: File) {
        _clips.update { current ->
            listOf(clipFromFile(videoFile)) + current.filter { it.videoPath != null }
        }
    }

    override fun visitKukistop(kukistopId: String) {
        val stop = _kukistops.value.firstOrNull { it.id == kukistopId } ?: return
        if (stop.visited) return
        _kukistops.update { list -> list.map { if (it.id == kukistopId) it.copy(visited = true) else it } }
        _profile.update { it.copy(kukistopsVisited = it.kukistopsVisited + 1) }
    }
}
