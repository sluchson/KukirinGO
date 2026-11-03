-- =====================================================================
-- kukirinGO — początkowy schemat bazy (Supabase / PostgreSQL + PostGIS)
-- Uruchomienie: `supabase db push` (opis w supabase/README.md)
-- =====================================================================

create extension if not exists postgis with schema extensions;

-- ---------------------------------------------------------------------
-- Profile użytkowników (1:1 z auth.users)
-- ---------------------------------------------------------------------
create table public.profiles (
    id          uuid primary key references auth.users (id) on delete cascade,
    nick        text not null unique check (char_length(nick) between 3 and 30),
    avatar_url  text,
    created_at  timestamptz not null default now()
);

-- Po rejestracji automatycznie tworzymy profil.
create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
    insert into public.profiles (id, nick)
    values (new.id, coalesce(new.raw_user_meta_data ->> 'nick', 'rider_' || left(new.id::text, 8)));
    return new;
end;
$$;

create trigger on_auth_user_created
    after insert on auth.users
    for each row execute function public.handle_new_user();

-- Obserwowanie znajomych
create table public.follows (
    follower_id uuid not null references public.profiles (id) on delete cascade,
    followee_id uuid not null references public.profiles (id) on delete cascade,
    created_at  timestamptz not null default now(),
    primary key (follower_id, followee_id),
    check (follower_id <> followee_id)
);

-- ---------------------------------------------------------------------
-- Przejazdy („Jedź i mierz”)
-- ---------------------------------------------------------------------
create table public.rides (
    id                uuid primary key default gen_random_uuid(),
    user_id           uuid not null references public.profiles (id) on delete cascade,
    title             text not null default 'Przejazd',
    started_at        timestamptz not null,
    ended_at          timestamptz,
    distance_m        numeric(10, 1) not null default 0 check (distance_m >= 0),
    duration_s        integer not null default 0 check (duration_s >= 0),
    avg_speed_kmh     numeric(5, 1),
    battery_used_pct  smallint check (battery_used_pct between 0 and 100),
    path              geography(LineString, 4326),
    visibility        text not null default 'public' check (visibility in ('public', 'followers', 'private')),
    created_at        timestamptz not null default now()
);
create index rides_user_idx on public.rides (user_id, started_at desc);

-- Propsy i komentarze („Dziel się”)
create table public.ride_props (
    ride_id    uuid not null references public.rides (id) on delete cascade,
    user_id    uuid not null references public.profiles (id) on delete cascade,
    created_at timestamptz not null default now(),
    primary key (ride_id, user_id)
);

create table public.ride_comments (
    id         uuid primary key default gen_random_uuid(),
    ride_id    uuid not null references public.rides (id) on delete cascade,
    user_id    uuid not null references public.profiles (id) on delete cascade,
    body       text not null check (char_length(body) between 1 and 500),
    created_at timestamptz not null default now()
);

-- ---------------------------------------------------------------------
-- Trasy polecane przez społeczność („Odkrywaj trasy”)
-- ---------------------------------------------------------------------
create table public.routes (
    id          uuid primary key default gen_random_uuid(),
    author_id   uuid not null references public.profiles (id) on delete cascade,
    name        text not null,
    description text,
    path        geography(LineString, 4326) not null,
    distance_m  numeric(10, 1),
    created_at  timestamptz not null default now()
);
create index routes_path_idx on public.routes using gist (path);

create table public.route_ratings (
    route_id       uuid not null references public.routes (id) on delete cascade,
    user_id        uuid not null references public.profiles (id) on delete cascade,
    surface_rating smallint not null check (surface_rating between 1 and 5),
    comment        text,
    created_at     timestamptz not null default now(),
    primary key (route_id, user_id)
);

-- ---------------------------------------------------------------------
-- Kukistopy (punkty na mapie do zaliczenia) i ich zaliczanie
-- ---------------------------------------------------------------------
create table public.kukistops (
    id          uuid primary key default gen_random_uuid(),
    name        text not null,
    description text,
    location    geography(Point, 4326) not null,
    radius_m    integer not null default 50 check (radius_m between 10 and 500),
    created_at  timestamptz not null default now()
);
create index kukistops_location_idx on public.kukistops using gist (location);

create table public.kukistop_visits (
    kukistop_id uuid not null references public.kukistops (id) on delete cascade,
    user_id     uuid not null references public.profiles (id) on delete cascade,
    visited_at  timestamptz not null default now(),
    primary key (kukistop_id, user_id)
);

-- Kukistopy w promieniu od pozycji użytkownika (wywołanie z aplikacji: rpc('nearby_kukistops')).
create or replace function public.nearby_kukistops(
    p_lat double precision,
    p_lng double precision,
    p_radius_m integer default 5000
)
returns table (id uuid, name text, description text, distance_m double precision, visited boolean)
language sql
stable
set search_path = public, extensions
as $$
    select k.id,
           k.name,
           k.description,
           st_distance(k.location, st_setsrid(st_makepoint(p_lng, p_lat), 4326)::geography) as distance_m,
           exists (
               select 1 from public.kukistop_visits v
               where v.kukistop_id = k.id and v.user_id = auth.uid()
           ) as visited
    from public.kukistops k
    where st_dwithin(k.location, st_setsrid(st_makepoint(p_lng, p_lat), 4326)::geography, p_radius_m)
    order by 4;
$$;

-- Zaliczenie kukistopu — serwer sprawdza, czy użytkownik jest w promieniu punktu.
create or replace function public.visit_kukistop(
    p_kukistop_id uuid,
    p_lat double precision,
    p_lng double precision
)
returns boolean
language plpgsql
security definer
set search_path = public, extensions
as $$
declare
    in_range boolean;
begin
    if auth.uid() is null then
        raise exception 'Musisz być zalogowany';
    end if;

    select st_dwithin(k.location, st_setsrid(st_makepoint(p_lng, p_lat), 4326)::geography, k.radius_m)
      into in_range
      from public.kukistops k
     where k.id = p_kukistop_id;

    if not coalesce(in_range, false) then
        return false;
    end if;

    insert into public.kukistop_visits (kukistop_id, user_id)
    values (p_kukistop_id, auth.uid())
    on conflict do nothing;
    return true;
end;
$$;

-- ---------------------------------------------------------------------
-- Zloty („Organizuj zloty”)
-- ---------------------------------------------------------------------
create table public.meetups (
    id                 uuid primary key default gen_random_uuid(),
    organizer_id       uuid not null references public.profiles (id) on delete cascade,
    title              text not null,
    description        text,
    starts_at          timestamptz not null,
    meeting_point_name text not null,
    meeting_point      geography(Point, 4326),
    route_id           uuid references public.routes (id) on delete set null,
    distance_km        numeric(5, 1),
    pace               text not null default 'spokojne tempo',
    max_participants   integer not null default 20 check (max_participants between 2 and 200),
    sweeper_id         uuid references public.profiles (id) on delete set null, -- osoba zamykająca grupę
    visibility         text not null default 'public' check (visibility in ('public', 'followers')),
    created_at         timestamptz not null default now()
);
create index meetups_starts_idx on public.meetups (starts_at);

create table public.meetup_participants (
    meetup_id uuid not null references public.meetups (id) on delete cascade,
    user_id   uuid not null references public.profiles (id) on delete cascade,
    joined_at timestamptz not null default now(),
    primary key (meetup_id, user_id)
);

-- Pilnuje limitu miejsc na zlocie.
create or replace function public.check_meetup_limit()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
    max_count     integer;
    current_count integer;
begin
    select max_participants into max_count
      from public.meetups where id = new.meetup_id
       for update;

    select count(*) into current_count
      from public.meetup_participants where meetup_id = new.meetup_id;

    if current_count >= max_count then
        raise exception 'Zlot jest pełny';
    end if;
    return new;
end;
$$;

create trigger meetup_limit
    before insert on public.meetup_participants
    for each row execute function public.check_meetup_limit();

-- Organizator automatycznie jest uczestnikiem.
create or replace function public.add_organizer_to_meetup()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
    insert into public.meetup_participants (meetup_id, user_id) values (new.id, new.organizer_id);
    return new;
end;
$$;

create trigger meetup_organizer_joins
    after insert on public.meetups
    for each row execute function public.add_organizer_to_meetup();

create or replace function public.is_meetup_participant(p_meetup_id uuid)
returns boolean
language sql
stable
security definer
set search_path = public
as $$
    select exists (
        select 1 from public.meetup_participants
        where meetup_id = p_meetup_id and user_id = auth.uid()
    );
$$;

-- Pozycje uczestników na żywo (mapa grupy podczas zlotu, przez Supabase Realtime).
create table public.meetup_live_positions (
    meetup_id  uuid not null references public.meetups (id) on delete cascade,
    user_id    uuid not null references public.profiles (id) on delete cascade,
    position   geography(Point, 4326) not null,
    updated_at timestamptz not null default now(),
    primary key (meetup_id, user_id)
);

-- ---------------------------------------------------------------------
-- kukirinTok — klipy z przejazdów
-- ---------------------------------------------------------------------
create table public.clips (
    id            uuid primary key default gen_random_uuid(),
    user_id       uuid not null references public.profiles (id) on delete cascade,
    ride_id       uuid references public.rides (id) on delete set null,
    video_path    text not null,             -- ścieżka w buckecie Storage „clips”
    thumbnail_path text,
    caption       text check (char_length(caption) <= 300),
    location_name text,
    duration_s    smallint check (duration_s between 1 and 60),
    created_at    timestamptz not null default now()
);
create index clips_created_idx on public.clips (created_at desc);

create table public.clip_likes (
    clip_id    uuid not null references public.clips (id) on delete cascade,
    user_id    uuid not null references public.profiles (id) on delete cascade,
    created_at timestamptz not null default now(),
    primary key (clip_id, user_id)
);

create table public.clip_comments (
    id         uuid primary key default gen_random_uuid(),
    clip_id    uuid not null references public.clips (id) on delete cascade,
    user_id    uuid not null references public.profiles (id) on delete cascade,
    body       text not null check (char_length(body) between 1 and 300),
    created_at timestamptz not null default now()
);

-- ---------------------------------------------------------------------
-- Odznaki i ranking
-- ---------------------------------------------------------------------
create table public.badges (
    id          text primary key,
    name        text not null,
    description text not null
);

create table public.user_badges (
    user_id   uuid not null references public.profiles (id) on delete cascade,
    badge_id  text not null references public.badges (id) on delete cascade,
    earned_at timestamptz not null default now(),
    primary key (user_id, badge_id)
);

-- Ranking tygodniowy: liczą się kilometry, a NIE prędkość.
create or replace view public.weekly_leaderboard
with (security_invoker = true)
as
select p.id                                  as user_id,
       p.nick,
       round(sum(r.distance_m) / 1000.0, 1)  as km,
       count(*)                              as rides
  from public.rides r
  join public.profiles p on p.id = r.user_id
 where r.started_at >= date_trunc('week', now())
   and r.visibility = 'public'
 group by p.id, p.nick;

-- =====================================================================
-- Row Level Security — kto może co czytać i zmieniać
-- =====================================================================
alter table public.profiles              enable row level security;
alter table public.follows               enable row level security;
alter table public.rides                 enable row level security;
alter table public.ride_props            enable row level security;
alter table public.ride_comments         enable row level security;
alter table public.routes                enable row level security;
alter table public.route_ratings         enable row level security;
alter table public.kukistops             enable row level security;
alter table public.kukistop_visits       enable row level security;
alter table public.meetups               enable row level security;
alter table public.meetup_participants   enable row level security;
alter table public.meetup_live_positions enable row level security;
alter table public.clips                 enable row level security;
alter table public.clip_likes            enable row level security;
alter table public.clip_comments         enable row level security;
alter table public.badges                enable row level security;
alter table public.user_badges           enable row level security;

-- Profile
create policy "profile: czytają zalogowani" on public.profiles
    for select to authenticated using (true);
create policy "profile: edytuje właściciel" on public.profiles
    for update to authenticated using (id = auth.uid()) with check (id = auth.uid());

-- Obserwowanie
create policy "follows: czytają zalogowani" on public.follows
    for select to authenticated using (true);
create policy "follows: dodaje obserwujący" on public.follows
    for insert to authenticated with check (follower_id = auth.uid());
create policy "follows: usuwa obserwujący" on public.follows
    for delete to authenticated using (follower_id = auth.uid());

-- Przejazdy: publiczne widzą wszyscy, „followers” obserwujący, prywatne tylko autor
create policy "rides: odczyt" on public.rides
    for select to authenticated using (
        user_id = auth.uid()
        or visibility = 'public'
        or (visibility = 'followers' and exists (
            select 1 from public.follows f
            where f.follower_id = auth.uid() and f.followee_id = rides.user_id
        ))
    );
create policy "rides: dodaje autor" on public.rides
    for insert to authenticated with check (user_id = auth.uid());
create policy "rides: edytuje autor" on public.rides
    for update to authenticated using (user_id = auth.uid()) with check (user_id = auth.uid());
create policy "rides: usuwa autor" on public.rides
    for delete to authenticated using (user_id = auth.uid());

-- Propsy i komentarze do przejazdów
create policy "ride_props: odczyt" on public.ride_props
    for select to authenticated using (true);
create policy "ride_props: dodaje siebie" on public.ride_props
    for insert to authenticated with check (user_id = auth.uid());
create policy "ride_props: usuwa siebie" on public.ride_props
    for delete to authenticated using (user_id = auth.uid());

create policy "ride_comments: odczyt" on public.ride_comments
    for select to authenticated using (true);
create policy "ride_comments: dodaje autor" on public.ride_comments
    for insert to authenticated with check (user_id = auth.uid());
create policy "ride_comments: usuwa autor" on public.ride_comments
    for delete to authenticated using (user_id = auth.uid());

-- Trasy i oceny
create policy "routes: odczyt" on public.routes
    for select to authenticated using (true);
create policy "routes: dodaje autor" on public.routes
    for insert to authenticated with check (author_id = auth.uid());
create policy "routes: edytuje autor" on public.routes
    for update to authenticated using (author_id = auth.uid()) with check (author_id = auth.uid());

create policy "route_ratings: odczyt" on public.route_ratings
    for select to authenticated using (true);
create policy "route_ratings: dodaje autor" on public.route_ratings
    for insert to authenticated with check (user_id = auth.uid());
create policy "route_ratings: edytuje autor" on public.route_ratings
    for update to authenticated using (user_id = auth.uid()) with check (user_id = auth.uid());

-- Kukistopy: czytają wszyscy zalogowani; dodaje tylko admin (service role).
create policy "kukistops: odczyt" on public.kukistops
    for select to authenticated using (true);
-- Zaliczenia: tylko przez funkcję visit_kukistop (sprawdza odległość).
create policy "kukistop_visits: odczyt" on public.kukistop_visits
    for select to authenticated using (true);

-- Zloty
create policy "meetups: odczyt" on public.meetups
    for select to authenticated using (
        organizer_id = auth.uid()
        or visibility = 'public'
        or exists (
            select 1 from public.follows f
            where f.follower_id = auth.uid() and f.followee_id = meetups.organizer_id
        )
    );
create policy "meetups: tworzy organizator" on public.meetups
    for insert to authenticated with check (organizer_id = auth.uid());
create policy "meetups: edytuje organizator" on public.meetups
    for update to authenticated using (organizer_id = auth.uid()) with check (organizer_id = auth.uid());
create policy "meetups: usuwa organizator" on public.meetups
    for delete to authenticated using (organizer_id = auth.uid());

create policy "meetup_participants: odczyt" on public.meetup_participants
    for select to authenticated using (true);
create policy "meetup_participants: zapis siebie" on public.meetup_participants
    for insert to authenticated with check (user_id = auth.uid());
create policy "meetup_participants: wypis siebie" on public.meetup_participants
    for delete to authenticated using (user_id = auth.uid());

-- Pozycje na żywo widzą i wysyłają tylko uczestnicy danego zlotu
create policy "live: odczyt uczestnicy" on public.meetup_live_positions
    for select to authenticated using (public.is_meetup_participant(meetup_id));
create policy "live: wysyła uczestnik" on public.meetup_live_positions
    for insert to authenticated with check (user_id = auth.uid() and public.is_meetup_participant(meetup_id));
create policy "live: aktualizuje uczestnik" on public.meetup_live_positions
    for update to authenticated using (user_id = auth.uid()) with check (user_id = auth.uid());
create policy "live: usuwa uczestnik" on public.meetup_live_positions
    for delete to authenticated using (user_id = auth.uid());

-- kukirinTok
create policy "clips: odczyt" on public.clips
    for select to authenticated using (true);
create policy "clips: dodaje autor" on public.clips
    for insert to authenticated with check (user_id = auth.uid());
create policy "clips: edytuje autor" on public.clips
    for update to authenticated using (user_id = auth.uid()) with check (user_id = auth.uid());
create policy "clips: usuwa autor" on public.clips
    for delete to authenticated using (user_id = auth.uid());

create policy "clip_likes: odczyt" on public.clip_likes
    for select to authenticated using (true);
create policy "clip_likes: dodaje siebie" on public.clip_likes
    for insert to authenticated with check (user_id = auth.uid());
create policy "clip_likes: usuwa siebie" on public.clip_likes
    for delete to authenticated using (user_id = auth.uid());

create policy "clip_comments: odczyt" on public.clip_comments
    for select to authenticated using (true);
create policy "clip_comments: dodaje autor" on public.clip_comments
    for insert to authenticated with check (user_id = auth.uid());
create policy "clip_comments: usuwa autor" on public.clip_comments
    for delete to authenticated using (user_id = auth.uid());

-- Odznaki (przyznaje backend / service role)
create policy "badges: odczyt" on public.badges
    for select to authenticated using (true);
create policy "user_badges: odczyt" on public.user_badges
    for select to authenticated using (true);

-- =====================================================================
-- Storage: bucket na klipy. Plik trafia do folderu <user_id>/nazwa.mp4
-- =====================================================================
insert into storage.buckets (id, name, public)
values ('clips', 'clips', true)
on conflict (id) do nothing;

create policy "clips storage: wgrywa właściciel folderu" on storage.objects
    for insert to authenticated
    with check (bucket_id = 'clips' and (storage.foldername(name))[1] = auth.uid()::text);
create policy "clips storage: usuwa właściciel folderu" on storage.objects
    for delete to authenticated
    using (bucket_id = 'clips' and (storage.foldername(name))[1] = auth.uid()::text);

-- =====================================================================
-- Realtime: zmiany pozycji na zlocie trafiają od razu do aplikacji
-- =====================================================================
alter publication supabase_realtime add table public.meetup_live_positions;
