# kukirinGO 🛴

Aplikacja na Androida dla osób jeżdżących na hulajnogach elektrycznych.
Nagrywasz przejazdy, zaliczasz kukistopy na mapie, organizujesz zloty z ekipą i wrzucasz klipy do **kukirinTok**.

| | |
|---|---|
| **Platforma** | Android (min. Android 8.0, API 26) |
| **Aplikacja** | Kotlin + Jetpack Compose (Material 3), Android Studio |
| **Backend** | [Supabase](https://supabase.com): PostgreSQL + PostGIS, logowanie, pliki (klipy), Realtime (mapa zlotu na żywo) |
| **CI** | GitHub Actions — każdy push i PR buduje APK i odpala testy |

## Zespół

| Osoba | Rola |
|---|---|
| Anna Kordybacha | Project Manager |
| Maciej Pereślucha | Frontend Developer |
| Mateusz Pokrywka | Frontend Developer |
| Wiktor Cioch | Backend Developer |
| Konrad Krupa | Backend Developer |

## Jak uruchomić (pierwszy raz)

1. Zainstaluj **[Android Studio](https://developer.android.com/studio)** (najnowsza stabilna wersja). Java jest w zestawie.
2. Sklonuj repo: **File → New → Project from Version Control** i wklej adres repozytorium z GitHuba.
3. Poczekaj, aż Android Studio zrobi **Gradle Sync** (pierwszy raz może trwać kilka minut — pobiera biblioteki).
4. Uruchom emulator (**Device Manager → Create Virtual Device**, np. Pixel 8, API 35) albo podłącz telefon z włączonym debugowaniem USB.
5. Kliknij zielone ▶ **Run 'app'**.

Na razie aplikacja działa na **danych przykładowych** (`FakeKukirinRepository`), więc nie potrzeba ani backendu, ani kluczy.

## Co już jest

- Motyw czarno-pomarańczowy, logo i ikona aplikacji (O jako opona 🛞), krój Exo 2
- Dolna nawigacja z 5 ekranami:
  - **Mapa** — podgląd mapy i lista kukistopów w okolicy
  - **Jazda** — start/stop nagrywania (na razie dane demo), ostatnie przejazdy
  - **Zloty** — lista zlotów, zapis/wypis z limitem miejsc
  - **kukirinTok** — pionowy feed klipów przewijany palcem, lajki
  - **Profil** — statystyki i odznaki
- Warstwa danych przez interfejs `KukirinRepository` — backend podmieni implementację bez ruszania ekranów
- Schemat bazy Supabase z zabezpieczeniami (RLS) — [`supabase/`](supabase/README.md)

## Struktura repo

```
app/src/main/java/pl/kukiringo/app/
├── MainActivity.kt
├── navigation/        # dolny pasek i nawigacja między ekranami
├── ui/
│   ├── theme/         # kolory, czcionki, motyw
│   ├── components/    # wspólne elementy (karty, kafelki statystyk)
│   └── screens/       # map, ride, meetups, clips, profile
└── data/
    ├── model/         # klasy danych (Ride, Meetup, Clip…)
    └── repository/    # KukirinRepository + dane przykładowe
supabase/
├── migrations/        # schemat bazy (SQL)
└── seed.sql           # dane startowe
docs/                  # architektura, plan prac
```

## Jak pracujemy

Zasady gałęzi, commitów i code review: **[CONTRIBUTING.md](CONTRIBUTING.md)**.
Plan prac z podziałem na osoby: **[docs/ROADMAP.md](docs/ROADMAP.md)**.
Architektura i model danych: **[docs/ARCHITEKTURA.md](docs/ARCHITEKTURA.md)**.
