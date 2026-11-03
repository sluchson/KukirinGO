package pl.kukiringo.app.data.model

/** Zapisany przejazd (docelowo tabela `rides` w Supabase). */
data class Ride(
    val id: String,
    val title: String,
    val dateLabel: String,
    val distanceKm: Double,
    val durationMin: Int,
    val avgSpeedKmh: Double,
    val batteryUsedPct: Int,
    val props: Int,
)

/** Punkt na mapie do „zaliczenia” — jak PokéStop w Pokémon GO (tabela `kukistops`). */
data class Kukistop(
    val id: String,
    val name: String,
    val description: String,
    val distanceKm: Double,
    val visited: Boolean,
)

/** Zlot, czyli wspólny przejazd (tabele `meetups` i `meetup_participants`). */
data class Meetup(
    val id: String,
    val title: String,
    val dateLabel: String,
    val meetingPoint: String,
    val distanceKm: Double,
    val pace: String,
    val participants: Int,
    val limit: Int,
    val joined: Boolean,
)

/** Krótki filmik z przejazdu w kukirinTok (tabela `clips` + plik w Storage). */
data class Clip(
    val id: String,
    val author: String,
    val caption: String,
    val location: String,
    val likes: Int,
    val comments: Int,
    val likedByMe: Boolean,
)

data class Badge(
    val name: String,
    val description: String,
    val earned: Boolean,
)

data class UserProfile(
    val nick: String,
    val totalKm: Double,
    val ridesCount: Int,
    val kukistopsVisited: Int,
    val badges: List<Badge>,
)
