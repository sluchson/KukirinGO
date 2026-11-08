-- Dane startowe: odznaki i kukistopy w Rzeszowie.
-- Współrzędne skopiowane z Google Maps. radius_m = promień (w metrach), w którym można zaliczyć punkt.

insert into public.badges (id, name, description) values
    ('km_100',      'Pierwsze 100 km', 'Przejedź łącznie 100 km'),
    ('night_owl',   'Nocny marek',     'Przejazd po 22:00'),
    ('meetups_5',   'Zlotowicz',       'Weź udział w 5 zlotach'),
    ('stops_50',    'Kolekcjoner',     'Zalicz 50 kukistopów'),
    ('first_clip',  'Reżyser',         'Wrzuć pierwszy klip do kukirinTok')
on conflict (id) do nothing;

insert into public.kukistops (name, description, location, radius_m) values
    ('Studnia na Rynku', 'Serce Rzeszowa — zbiórka większości zlotów',
        st_setsrid(st_makepoint(22.0049323, 50.0375035), 4326)::geography, 60),
    ('Zamek Lubomirskich', 'Objedź zamek dookoła',
        st_setsrid(st_makepoint(22.0006347, 50.0324847), 4326)::geography, 80),
    ('Pomnik Czynu Rewolucyjnego', 'Najbardziej rozpoznawalny punkt w mieście',
        st_setsrid(st_makepoint(21.9994961, 50.0406072), 4326)::geography, 60),
    ('Rzeszowskie Bulwary', 'Najlepsza nawierzchnia nad Wisłokiem',
        st_setsrid(st_makepoint(22.0020559, 50.0262763), 4326)::geography, 150),
    ('Zalew Wisłok', 'Długa prosta wzdłuż wody',
        st_setsrid(st_makepoint(21.9951959, 50.0135542), 4326)::geography, 300),
    ('Politechnika Rzeszowska', 'Kampus przy al. Powstańców Warszawy',
        st_setsrid(st_makepoint(21.989195, 50.019034), 4326)::geography, 150),
    ('Wydział Elektrotechniki i Informatyki PRz', 'Zalicz po wykładzie',
        st_setsrid(st_makepoint(21.9851869, 50.0270339), 4326)::geography, 120);
