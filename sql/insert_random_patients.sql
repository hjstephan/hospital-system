-- Insert 1000 randomly generated patients into `public.patients`.
-- Run this script with psql against your hospital database, e.g.:
--   psql -U hospital_admin -d hospital_db -f scripts/insert_random_patients.sql
--
-- The script uses PostgreSQL functions (random(), generate_series()). It omits the `id` column
-- so the table sequence (patients_id_seq) will assign ids. If your DB doesn't have the
-- default on patients.id, either add the default or set id explicitly.

BEGIN;

INSERT INTO public.patients (
  first_name, last_name, date_of_birth, gender, phone, email, address,
  insurance_number, blood_type, allergies, emergency_contact_name, emergency_contact_phone,
  admission_date, discharge_date, status, created_at, updated_at,
  epa_id, epa_sync_status, epa_last_sync, epa_sync_error, epa_enabled, epa_consent_date
)
SELECT
  -- first / last name
  (ARRAY['Anna','Max','Sophie','Lukas','Maria','Paul','Laura','Jonas','Lena','Tim','Julia','Leon','Eva','Noah','Emil','Sarah','Felix','Lisa','Michael','Katharina'])[(floor(random()*20)+1)::int],
  (ARRAY['Müller','Schmidt','Schneider','Fischer','Weber','Meyer','Wagner','Becker','Hoffmann','Schulz','Braun','Zimmermann','Schäfer','Koch','Richter','Klein','Wolf','Neumann','Schwarz','Lange'])[(floor(random()*20)+1)::int],

  -- date of birth between 1920-01-01 and 2005-12-31
  (date '1920-01-01' + (floor(random() * (date '2005-12-31' - date '1920-01-01')) )::int),

  -- gender (German labels used by the app)
  (ARRAY['Männlich','Weiblich','Divers'])[(floor(random()*3)+1)::int],

  -- phone (German-like)
  ('030-' || (10000000 + floor(random()*90000000)::int)::text),

  -- simple generated email (lowercase)
  lower( ( (ARRAY['anna','max','sophie','lukas','maria','paul','laura','jonas','lena','tim','julia','leon','eva','noah','emil','sarah','felix','lisa','michael','katharina'])[(floor(random()*20)+1)::int] ) || '.' ||
         ( (ARRAY['mueller','schmidt','schneider','fischer','weber','meyer','wagner','becker','hoffmann','schulz','braun','zimmermann','schaefer','koch','richter','klein','wolf','neumann','schwarz','lange'])[(floor(random()*20)+1)::int] ) ||
         (floor(random()*900+100)::int)::text || '@example.com'),

  -- address: street + number
  ( (ARRAY['Hauptstraße','Nebenstraße','Parkweg','Bahnhofstraße','Gartenweg','Luisenstraße','Bergweg','Lindenstraße','Schulstraße','Rosenweg'])[(floor(random()*10)+1)::int] || ' ' || (floor(random()*200+1)::int)::text ),

  -- insurance number: unique-ish based on series index
  ('INS-' || to_char(current_date,'YYYY') || '-' || lpad(s::text,6,'0')),

  -- blood type
  (ARRAY['A+','A-','B+','B-','AB+','AB-','O+','O-'])[(floor(random()*8)+1)::int],

  -- allergies (mostly 'Keine')
  (CASE WHEN random() < 0.25 THEN (ARRAY['Penicillin','Aspirin','Pollen','Latex','Nüsse'])[(floor(random()*5)+1)::int] ELSE 'Keine' END),

  -- emergency contact name
  ( (ARRAY['Hans','Helga','Peter','Sabine','Thomas','Maria','Klaus','Franz','Ingrid','Ute'])[(floor(random()*10)+1)::int] || ' ' ||
    (ARRAY['Muster','Schulz','Schneider','Fischer','Becker','Klein','Wagner','Weber','Richter','Neumann'])[(floor(random()*10)+1)::int] ),

  -- emergency contact phone
  ('030-' || (10000000 + floor(random()*90000000)::int)::text),

  -- admission_date (random within last ~730 days)
  (now() - (floor(random()*730)::int || ' days')::interval),

  -- discharge_date (20% chance set to a recent date; otherwise NULL)
  (CASE WHEN random() < 0.20 THEN (now() - (floor(random()*365)::int || ' days')::interval) ELSE NULL END),

  -- status: mostly active
  (CASE WHEN random() < 0.80 THEN 'active' ELSE 'discharged' END),

  -- created_at, updated_at
  (now() - (floor(random()*400)::int || ' days')::interval),
  (now() - (floor(random()*10)::int || ' days')::interval),

  -- epa_id: present for some patients
  (CASE WHEN random() < 0.7 THEN ('EPA-' || substr(md5(random()::text),1,12)) ELSE NULL END),

  -- epa_sync_status
  (CASE
     WHEN random() < 0.6 THEN (ARRAY['pending','synced','error'])[(floor(random()*3)+1)::int]
     WHEN random() < 0.85 THEN 'disabled'
     ELSE 'pending'
   END),

  -- epa_last_sync (50% chance)
  (CASE WHEN random() < 0.5 THEN (now() - (floor(random()*120)::int || ' days')::interval) ELSE NULL END),

  -- epa_sync_error (rare)
  (CASE WHEN random() < 0.05 THEN (ARRAY['Timeout','Invalid FHIR payload','401 Unauthorized','Server error'])[ (floor(random()*4)+1)::int ] ELSE NULL END),

  -- epa_enabled
  (random() < 0.8),

  -- epa_consent_date (if enabled)
  (CASE WHEN random() < 0.8 THEN (now() - (floor(random()*800)::int || ' days')::interval) ELSE NULL END)

FROM generate_series(1,1000) AS s;

COMMIT;

-- Optional: reset patients_id_seq if necessary so nextval aligns with max(id)
-- SELECT setval('public.patients_id_seq', COALESCE((SELECT MAX(id) FROM public.patients), 1));

-- End of script
