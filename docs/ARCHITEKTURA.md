# Architektura

## Ogólnie

```mermaid
flowchart LR
    subgraph Telefon["Aplikacja Android (Kotlin + Compose)"]
        UI[Ekrany Compose] --> VM[ViewModel]
        VM --> Repo[KukirinRepository]
        GPS[GPS / FusedLocation] --> VM
        Cam[CameraX] --> VM
    end
    subgraph Supabase
        Auth[Auth — logowanie]
        DB[(PostgreSQL + PostGIS)]
        St[Storage — klipy]
        RT[Realtime — zlot na żywo]
    end
    Repo -- supabase-kt --> Auth
    Repo --> DB
    Repo --> St
    RT -- pozycje grupy --> Repo
```

- **Ekrany** (Compose) tylko wyświetlają stan i przekazują kliknięcia.
- **ViewModel** trzyma stan ekranu i logikę (np. nagrywanie przejazdu).
- **KukirinRepository** to jedyne miejsce, które rozmawia z backendem. Teraz: `FakeKukirinRepository`, docelowo `SupabaseKukirinRepository`.
- **Supabase** daje gotowe logowanie, bazę, pliki i realtime — backend skupia się na schemacie, regułach dostępu (RLS) i funkcjach SQL.

## Model danych

```mermaid
erDiagram
    profiles ||--o{ rides : "nagrywa"
    profiles ||--o{ follows : "obserwuje"
    rides ||--o{ ride_props : "propsy"
    rides ||--o{ ride_comments : "komentarze"
    profiles ||--o{ routes : "poleca"
    routes ||--o{ route_ratings : "oceny"
    kukistops ||--o{ kukistop_visits : "zaliczenia"
    profiles ||--o{ kukistop_visits : ""
    profiles ||--o{ meetups : "organizuje"
    meetups ||--o{ meetup_participants : "uczestnicy"
    meetups ||--o{ meetup_live_positions : "pozycje na żywo"
    routes |o--o{ meetups : "trasa zlotu"
    profiles ||--o{ clips : "kukirinTok"
    clips ||--o{ clip_likes : ""
    clips ||--o{ clip_comments : ""
    badges ||--o{ user_badges : ""
    profiles ||--o{ user_badges : ""
```

## Ważne decyzje

| Decyzja | Dlaczego |
|---|---|
| Natywny Android (Kotlin + Compose) | Nikt nie ma Maca, a GPS w tle, kamera i mapy działają najpewniej natywnie. Kotlin i Compose to aktualny standard Google. |
| Supabase zamiast własnego serwera | Darmowy plan, gotowe logowanie, PostGIS do zapytań „co jest w pobliżu”, Realtime do mapy zlotu, Storage na filmiki. Mniej pracy przy serwerze, więcej przy funkcjach. |
| Rankingi bez prędkości | Nie zachęcamy do szybkiej jazdy — liczą się kilometry i regularność. |
| Zaliczanie kukistopów tylko przez funkcję w bazie | Serwer sprawdza odległość od punktu, więc nie da się „zaliczyć” go z kanapy przez zwykły zapis do tabeli. |

## Biblioteki do dodania w kolejnych sprintach

| Do czego | Biblioteka |
|---|---|
| Połączenie z Supabase | `io.github.jan-tennert.supabase` (supabase-kt) + Ktor client |
| Mapa (OpenStreetMap, bez klucza API) | `org.osmdroid:osmdroid-android` |
| GPS | `com.google.android.gms:play-services-location` |
| Nagrywanie klipów | CameraX (`androidx.camera:camera-video`) |
| Odtwarzanie klipów | Media3 ExoPlayer (`androidx.media3:media3-exoplayer`) |
| Obrazki z sieci | Coil (`io.coil-kt.coil3:coil-compose`) |

Aktualne wersje sprawdzajcie na stronach bibliotek i dopisujcie do `gradle/libs.versions.toml`.
