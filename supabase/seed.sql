-- Dane startowe: odznaki i kilka przykładowych kukistopów.
-- Współrzędne są przykładowe (centrum Warszawy) — podmieńcie na punkty ze swojego miasta.

insert into public.badges (id, name, description) values
    ('km_100',      'Pierwsze 100 km', 'Przejedź łącznie 100 km'),
    ('night_owl',   'Nocny marek',     'Przejazd po 22:00'),
    ('meetups_5',   'Zlotowicz',       'Weź udział w 5 zlotach'),
    ('stops_50',    'Kolekcjoner',     'Zalicz 50 kukistopów'),
    ('first_clip',  'Reżyser',         'Wrzuć pierwszy klip do kukirinTok')
on conflict (id) do nothing;

insert into public.kukistops (name, description, location, radius_m) values
    ('Rynek Starego Miasta', 'Serce miasta, zbiórka większości zlotów',
        st_setsrid(st_makepoint(21.0122, 52.2497), 4326)::geography, 60),
    ('Bulwary nad Wisłą', 'Najlepsza nawierzchnia w okolicy',
        st_setsrid(st_makepoint(21.0200, 52.2440), 4326)::geography, 80),
    ('Pałac Kultury', 'Punkt orientacyjny — widać go z daleka',
        st_setsrid(st_makepoint(21.0067, 52.2318), 4326)::geography, 70);
