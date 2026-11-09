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
- **Mapa** — prawdziwa mapa Rzeszowa (OpenStreetMap), 7 kukistopów jako pinezki, Twoja pozycja z GPS, odległości na żywo i przycisk **Zalicz**, gdy jesteś w promieniu punktu
- **Jazda** — nagrywanie przejazdu z GPS: czas, dystans, prędkość i maks. prędkość na żywo, trasa rysowana na mapie, zapis przejazdu do listy i statystyk (ekran nie gaśnie podczas jazdy)
- **Zloty** — lista zlotów, zapis/wypis z limitem miejsc
- **kukirinTok** — nagrywanie klipu aparatem telefonu (do 60 s) albo dodanie filmu z galerii, pionowy feed z odtwarzaniem, lajki
- **Profil** — statystyki (rosną po każdym przejeździe i zaliczonym kukistopie) i odznaki
- Warstwa danych przez interfejs `KukirinRepository` — backend podmieni implementację bez ruszania ekranów
- Schemat bazy Supabase z zabezpieczeniami (RLS) i kukistopami w Rzeszowie — [`supabase/`](supabase/README.md)

> Na razie dane (poza przejazdami z GPS i klipami zapisanymi na telefonie) są przykładowe i żyją w pamięci aplikacji. Połączenie z Supabase — patrz [docs/ROADMAP.md](docs/ROADMAP.md).

## Struktura repo

```
app/src/main/java/pl/kukiringo/app/
├── MainActivity.kt
├── KukirinApplication.kt  # start aplikacji, konfiguracja mapy
├── location/          # GPS i nagrywanie przejazdu
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
