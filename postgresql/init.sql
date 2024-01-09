ALTER TABLE IF EXISTS antarctica.activities DROP CONSTRAINT fk_expedition_id;
ALTER TABLE IF EXISTS antarctica.cruises DROP CONSTRAINT fk_expedition_id;
ALTER TABLE IF EXISTS antarctica.expeditions DROP CONSTRAINT fk_cruise_line_id;

DROP TABLE IF EXISTS antarctica.activities;
DROP TABLE IF EXISTS antarctica.cruises;
DROP TABLE IF EXISTS antarctica.expeditions;
DROP TABLE IF EXISTS antarctica.cruise_lines;

DROP SCHEMA IF EXISTS antarctica;

CREATE SCHEMA antarctica;

CREATE TABLE antarctica.cruise_lines (
  cruise_line_id SERIAL,
  name VARCHAR(50) NOT NULL UNIQUE,
  website VARCHAR(255) NOT NULL UNIQUE,
  expedition_website TEXT NOT NULL UNIQUE,
  PRIMARY KEY (cruise_line_id)
);

CREATE TABLE antarctica.expeditions (
  expedition_id SERIAL,
  cruise_line_id INTEGER NOT NULL,
  website VARCHAR(255) NOT NULL,
  name VARCHAR(255) NOT NULL,
  description TEXT NOT NULL,
  departing_from VARCHAR(100) NOT NULL,
  arriving_at VARCHAR(100),
  duration VARCHAR(50) NOT NULL,
  starting_price DECIMAL(10, 4),
  photo_url TEXT,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  PRIMARY KEY (expedition_id),
  CONSTRAINT fk_cruise_line_id FOREIGN KEY (cruise_line_id) REFERENCES antarctica.cruise_lines (cruise_line_id) ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE antarctica.cruises (
  cruise_id SERIAL,
  expedition_id INTEGER NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  PRIMARY KEY (cruise_id),
  CONSTRAINT fk_expedition_id FOREIGN KEY (expedition_id) REFERENCES antarctica.expeditions (expedition_id) ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE antarctica.activities (
  activity_id SERIAL,
  expedition_id INTEGER NOT NULL,
  name VARCHAR(150) NOT NULL,
  description TEXT,
  is_included BOOLEAN NOT NULL,
  price DECIMAL(10, 4),
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  PRIMARY KEY (activity_id),
  CONSTRAINT fk_expedition_id FOREIGN KEY (expedition_id) REFERENCES antarctica.expeditions (expedition_id) ON DELETE CASCADE ON UPDATE NO ACTION
);

--CREATE TABLE antarctica.vessels (
--  vessel_id SERIAL,
--  name VARCHAR NOT NULL,
--  capacity INTEGER NOT NULL,
--  created_at TIMESTAMP NOT NULL,
--  updated_at TIMESTAMP NOT NULL,
--  PRIMARY KEY (vessel_id)
--);

DO $$
DECLARE
  cruise_lines VARCHAR[] := ARRAY['Lindblad Expeditions', 'Quark Expeditions', 'Viking Expeditions', 'Aurora Expeditions', 'Seaborn Expeditions', 'Ponant', 'Hurtigruten Expeditions'];
  home_websites VARCHAR[] := ARRAY[
    'https://world.expeditions.com',
    'https://www.quarkexpeditions.com',
    'https://www.vikingcruises.com',
    'https://www.aurora-expeditions.com/destination',
    'https://www.seabourn.com/en/cruise-destinations/expedition',
    'https://us.ponant.com/ponant-expeditions',
    'https://www.hurtigruten.com/en-us/expeditions'
   ];
  expedition_websites VARCHAR[] := ARRAY[
    'https://world.expeditions.com/book?destinations.name=Antarctica',
    'https://www.quarkexpeditions.com/expeditions?f%5B0%5D=expedition_region%3Aantarctic',
    'https://www.vikingcruises.com/expeditions/search-cruises/index.html?Regions=Antarctica',
    'https://www.aurora-expeditions.com/find-an-expedition/?destinations%5B%5D=antarctica-cruises&destinations%5B%5D=antarctic-peninsula&destinations%5B%5D=weddell-sea&destinations%5B%5D=south-georgia-island&destinations%5B%5D=falkland-islands-malvinas&destinations%5B%5D=antarctic-circle',
    'https://www.seabourn.com/en/find-a-cruise?destinationIds:(S)',
    'https://us.ponant.com/cruises/themes/polar-expedition?pred-facet-destination%5B%5D=ANTARCTI',
    'https://www.hurtigruten.com/en-us/expeditions/cruises/?forceRefresh=true&destinations=antarctica-cruises'
   ];
  i INTEGER;
BEGIN
  FOR i IN 1..array_length(cruise_lines, 1) LOOP
    INSERT INTO antarctica.cruise_lines (name, website, expedition_website)
    VALUES (cruise_lines[i], home_websites[i], expedition_websites[i]);
  END LOOP;
END $$;
