# Jak pracujemy w repo

Pięć osób w jednym repo = trzeba kilku prostych zasad, żeby nie nadpisywać sobie pracy.

## 1. Nikt nie pushuje prosto na `main`

`main` zawsze ma działać. Każda zmiana idzie przez **gałąź → Pull Request → review → merge**.

Ustawienie (robi właściciel repo, raz): **Settings → Branches → Add branch ruleset** dla `main`:
- ✅ Require a pull request before merging (1 approval)
- ✅ Require status checks to pass → `build` (z GitHub Actions)

## 2. Jedno zadanie = jedna gałąź

Każde zadanie ma **issue** na tablicy projektu (GitHub Projects). Z issue robimy gałąź:

| Rodzaj | Nazwa gałęzi | Przykład |
|---|---|---|
| nowa funkcja | `feature/<nr>-krotki-opis` | `feature/12-ekran-logowania` |
| poprawka | `fix/<nr>-krotki-opis` | `fix/18-crash-na-mapie` |
| backend/baza | `db/<nr>-krotki-opis` | `db/7-tabela-zlotow` |
| dokumentacja | `docs/<nr>-krotki-opis` | `docs/3-readme` |

```bash
git checkout main
git pull
git checkout -b feature/12-ekran-logowania
# ...praca...
git add .
git commit -m "Ekran logowania e-mailem"
git push -u origin feature/12-ekran-logowania
```

Potem na GitHubie: **Compare & pull request**. W opisie PR wpisz `Closes #12` — issue zamknie się samo po merge.

## 3. Commity

- Krótki opis po polsku, co się zmieniło: `Zapis na zlot`, `Poprawka licznika czasu po pauzie`.
- Jeden commit = jeden moduł albo jedna poprawka.
- Gdy nad modułem pracowało kilka osób, dopisujemy współautorów w **jednym** bloku na końcu opisu:

```powershell
git commit -m "Ekran Zloty" -m "Co-authored-by: Mateusz Pokrywka <113377009+MATPOK@users.noreply.github.com>`nCo-authored-by: Konrad Krupa <206438631+Kaczucha24@users.noreply.github.com>"
```

| Osoba | Adres do `Co-authored-by` |
|---|---|
| Anna Kordybacha | `149712185+Airashii123@users.noreply.github.com` |
| Mateusz Pokrywka | `113377009+MATPOK@users.noreply.github.com` |
| Wiktor Cioch | `176962127+WCiovh@users.noreply.github.com` |
| Konrad Krupa | `206438631+Kaczucha24@users.noreply.github.com` |

## 4. Code review

- Każdy PR przegląda **co najmniej 1 osoba** (najlepiej z tej samej „strony”: frontend ↔ frontend, backend ↔ backend).
- PR-y małe: lepiej 3 małe niż 1 gigantyczny.
- Przed PR: zrób `git pull origin main` do swojej gałęzi i sprawdź, że aplikacja się buduje.
- Zielony ✅ z GitHub Actions = buduje się. Czerwony ❌ = najpierw napraw.

## 5. Czego NIE wrzucamy do repo

- `local.properties` (ścieżka SDK, klucze API — każdy ma swój)
- klucze Supabase **service_role** — nigdy, w żadnym pliku
- folder `build/`, pliki `.idea/` z ustawieniami osobistymi (jest `.gitignore`)

Adres projektu i klucz **publishable** z Supabase trzymamy w `local.properties` (klucza `secret` nie wpisujemy nigdzie):

```properties
SUPABASE_URL=https://xxxx.supabase.co
SUPABASE_PUBLISHABLE_KEY=sb_publishable_...
```

## 6. Konflikty

Jeśli `git pull` pokazuje konflikt — nie panikuj. Android Studio: **Git → Resolve Conflicts**, wybierz właściwe fragmenty, commit. W razie wątpliwości zapytaj autora drugiej zmiany.
