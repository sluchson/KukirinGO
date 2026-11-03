# Backend — Supabase

## Co tu jest

- `migrations/20261103120000_init.sql` — cały schemat: tabele, indeksy, funkcje, reguły dostępu (RLS), bucket na klipy, Realtime
- `seed.sql` — odznaki i 3 przykładowe kukistopy (podmieńcie współrzędne na swoje miasto)

Migracja była przetestowana lokalnie na PostgreSQL 16 (z zaślepkami PostGIS): limit miejsc na zlocie,
blokada dodawania przejazdów za kogoś, pozycje na żywo tylko dla uczestników, wgrywanie klipów tylko do własnego folderu.

## Pierwsze uruchomienie (robi jedna osoba)

1. Załóż konto na <https://supabase.com> → **New project** (region: Frankfurt, plan Free).
2. Zainstaluj Supabase CLI: <https://supabase.com/docs/guides/local-development/cli/getting-started>
3. W folderze repo:

   ```bash
   supabase login
   supabase init            # utworzy supabase/config.toml (commitujemy go)
   supabase link --project-ref <ref-z-ustawień-projektu>
   supabase db push         # wgrywa migracje
   ```

4. Dane startowe: w panelu **SQL Editor** wklej i uruchom `seed.sql`.
5. **Project Settings → API**: skopiuj `Project URL` i klucz `anon` — każdy wpisuje je do swojego `local.properties` (plik `local.properties` w głównym folderze projektu, nie trafia do repo).
6. **Project Settings → Team**: zaproś resztę zespołu.

> Bez CLI też się da: wklej treść migracji w **SQL Editor** i uruchom.

## Nowa zmiana w bazie

Nigdy nie zmieniamy starej migracji, tylko dodajemy nową:

```bash
supabase migration new nazwa_zmiany   # tworzy nowy plik w migrations/
# edytujesz plik SQL
supabase db push
```

## Najważniejsze funkcje (wywołanie z aplikacji przez `rpc`)

| Funkcja | Co robi |
|---|---|
| `nearby_kukistops(p_lat, p_lng, p_radius_m)` | kukistopy w promieniu, z odległością i informacją, czy zaliczony |
| `visit_kukistop(p_kukistop_id, p_lat, p_lng)` | zalicza kukistop, jeśli użytkownik jest w jego promieniu |
| widok `weekly_leaderboard` | ranking tygodnia po kilometrach |
