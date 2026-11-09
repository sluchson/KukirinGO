# Plan prac (propozycja)

Podział na sprinty po ~2 tygodnie. Przypisania to propozycja — Anna (PM) może je zmienić na tablicy projektu.
Frontend = **Maciej, Mateusz**, backend = **Wiktor, Konrad**, PM = **Anna**.

## Sprint 0 — start (ten tydzień)

| Zadanie | Kto |
|---|---|
| Repo na GitHubie, zaproszenie wszystkich, ochrona gałęzi `main` | Anna |
| Tablica GitHub Projects (kolumny: Do zrobienia / W trakcie / Review / Gotowe), issue z tego planu | Anna |
| Każdy: instaluje Android Studio, klonuje repo, odpala aplikację na emulatorze | wszyscy |
| Projekt w Supabase (darmowy plan), wgranie migracji i seeda, zaproszenie Konrada | Wiktor |
| Przejrzenie schematu bazy, lista poprawek | Konrad |

## Sprint 1 — konto i nagrywanie przejazdu

| Zadanie | Kto |
|---|---|
| Biblioteka supabase-kt w aplikacji, `SupabaseKukirinRepository` (odczyt przejazdów i profilu) | Wiktor |
| Logowanie i rejestracja (e-mail + hasło) — logika | Konrad |
| Ekrany logowania i rejestracji | Mateusz |
| Prawdziwy GPS w `RideViewModel` (zgoda na lokalizację, FusedLocationProvider, foreground service) | Maciej |
| Zapis przejazdu do bazy po „Stop” (dystans, czas, trasa) | Wiktor + Maciej |

## Sprint 2 — mapa i kukistopy

| Zadanie | Kto |
|---|---|
| Mapa Rzeszowa (OpenStreetMap / osmdroid) na ekranie Mapa, pozycja użytkownika | Maciej |
| Kukistopy na mapie z `nearby_kukistops`, zaliczanie przez `visit_kukistop` | Maciej + Wiktor |
| Trasy społeczności i oceny nawierzchni (tabele `routes`, `route_ratings`) | Konrad |
| Ekran szczegółów przejazdu (mapa trasy, statystyki, propsy, komentarze) | Mateusz |

## Sprint 3 — zloty

| Zadanie | Kto |
|---|---|
| Formularz „Utwórz zlot” (trasa, data, zbiórka, limit, osoba zamykająca) | Mateusz |
| Zapis/wypis, lista uczestników — podpięcie do bazy | Konrad |
| Mapa grupy na żywo (Supabase Realtime, `meetup_live_positions`) | Konrad + Maciej |
| Powiadomienie przed zlotem | Wiktor |

## Sprint 4 — kukiTok

| Zadanie | Kto |
|---|---|
| Nagrywanie klipu (CameraX, max 60 s) | Mateusz |
| Wysyłka do Storage (bucket `clips`), zapis w tabeli `clips` | Wiktor |
| Odtwarzanie w feedzie (Media3 ExoPlayer), lajki i komentarze | Maciej |
| Moderacja: zgłaszanie klipów | Konrad |

## Sprint 5 — dopięcie

| Zadanie | Kto |
|---|---|
| Odznaki (przyznawanie po stronie bazy) i ranking tygodniowy | Konrad |
| Testy na kilku telefonach, lista błędów, poprawki | wszyscy |
| Dokumentacja, prezentacja końcowa, demo | Anna |
