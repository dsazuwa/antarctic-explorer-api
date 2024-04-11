ALTER TABLE IF EXISTS antarctic.expeditions_extensions DROP CONSTRAINT IF EXISTS fk_extension_id;
ALTER TABLE IF EXISTS antarctic.expeditions_extensions DROP CONSTRAINT IF EXISTS fk_expedition_id;
ALTER TABLE IF EXISTS antarctic.extensions DROP CONSTRAINT IF EXISTS fk_cruise_line_id;
ALTER TABLE IF EXISTS antarctic.departures DROP CONSTRAINT IF EXISTS fk_itinerary_id;
ALTER TABLE IF EXISTS antarctic.departures DROP CONSTRAINT IF EXISTS fk_vessel_id;
ALTER TABLE IF EXISTS antarctic.departures DROP CONSTRAINT IF EXISTS fk_expedition_id;
ALTER TABLE IF EXISTS antarctic.itineraries DROP CONSTRAINT IF EXISTS fk_expedition_id;
ALTER TABLE IF EXISTS antarctic.vessels DROP CONSTRAINT IF EXISTS fk_cruise_line_id;
ALTER TABLE IF EXISTS antarctic.gallery DROP CONSTRAINT IF EXISTS fk_expedition;
ALTER TABLE IF EXISTS antarctic.expeditions DROP CONSTRAINT IF EXISTS fk_cruise_line_id;

DROP TABLE IF EXISTS antarctic.expeditions_extensions;
DROP TABLE IF EXISTS antarctic.extensions;
DROP TABLE IF EXISTS antarctic.departures;
DROP TABLE IF EXISTS antarctic.itinerary_details;
DROP TABLE IF EXISTS antarctic.itineraries;
DROP TABLE IF EXISTS antarctic.vessels;
DROP TABLE IF EXISTS antarctic.gallery;
DROP TABLE IF EXISTS antarctic.expeditions;
DROP TABLE IF EXISTS antarctic.cruise_lines;

DROP SCHEMA IF EXISTS antarctic;

CREATE SCHEMA IF NOT EXISTS antarctic;

CREATE TABLE antarctic.cruise_lines (
  cruise_line_id SERIAL,
  name VARCHAR(50) NOT NULL UNIQUE,
  website VARCHAR(255) NOT NULL UNIQUE,
  fleet_website VARCHAR(255) UNIQUE,
  expedition_website TEXT NOT NULL UNIQUE,
  logo TEXT NOT NULL UNIQUE,
  PRIMARY KEY (cruise_line_id)
);

CREATE TABLE antarctic.expeditions (
  expedition_id SERIAL,
  cruise_line_id INTEGER NOT NULL,
  website TEXT,
  name VARCHAR(255) NOT NULL,
  description TEXT[],
  highlights TEXT[],
  departing_from VARCHAR(100),
  arriving_at VARCHAR(100),
  duration VARCHAR(50) NOT NULL,
  starting_price DECIMAL(10, 4),
  photo_url TEXT,
  PRIMARY KEY (expedition_id),
  CONSTRAINT fk_cruise_line_id FOREIGN KEY (cruise_line_id) REFERENCES antarctic.cruise_lines (cruise_line_id) ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE antarctic.gallery (
  photo_id SERIAL,
  expedition_id INTEGER NOT NULL,
  photo_url TEXT NOT NULL,
  alt TEXT,
  PRIMARY KEY (photo_id),
  CONSTRAINT fk_expedition_id FOREIGN KEY (expedition_id) REFERENCES antarctic.expeditions (expedition_id) ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE antarctic.vessels (
  vessel_id SERIAL,
  cruise_line_id INTEGER NOT NULL,
  name VARCHAR NOT NULL,
  capacity INTEGER NOT NULL,
  cabins INTEGER,
  description TEXT[],
  website TEXT,
  photo_url TEXT NOT NULL,
  PRIMARY KEY (vessel_id),
  CONSTRAINT fk_cruise_line_id FOREIGN KEY (cruise_line_id) REFERENCES antarctic.cruise_lines (cruise_line_id) ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE antarctic.itineraries (
  itinerary_id SERIAL,
  expedition_id INTEGER NOT NULL,
  name VARCHAR(255),
  departing_from VARCHAR(100),
  arriving_at VARCHAR(100),
  duration VARCHAR(10) NOT NULL,
  map_url TEXT,
  PRIMARY KEY (itinerary_id),
  CONSTRAINT fk_expedition_id FOREIGN KEY (expedition_id) REFERENCES antarctic.expeditions (expedition_id) ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE antarctic.itinerary_details (
  detail_id SERIAL,
  itinerary_id INTEGER NOT NULL,
  day VARCHAR(10) NOT NULL,
  header VARCHAR(255) NOT NULL,
  content TEXT[] NOT NULL,
  PRIMARY KEY (detail_id),
  CONSTRAINT fk_itinerary_id FOREIGN KEY (itinerary_id) REFERENCES antarctic.itineraries (itinerary_id) ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE antarctic.departures (
  departure_id SERIAL,
  expedition_id INTEGER NOT NULL,
  vessel_id INTEGER NOT NULL,
  itinerary_id INTEGER NOT NULL,
  name VARCHAR,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  starting_price DECIMAL(10, 4),
  discounted_price DECIMAL(10, 4),
  website TEXT,
  PRIMARY KEY (departure_id),
  CONSTRAINT fk_expedition_id FOREIGN KEY (expedition_id) REFERENCES antarctic.expeditions (expedition_id) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT fk_vessel_id FOREIGN KEY (vessel_id) REFERENCES antarctic.vessels (vessel_id) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT fk_itinerary_id FOREIGN KEY (itinerary_id) REFERENCES antarctic.itineraries (itinerary_id) ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE antarctic.extensions (
  extension_id SERIAL,
  cruise_line_id INTEGER NOT NULL,
  name VARCHAR NOT NULL, 
  starting_price DECIMAL(10, 4),
  duration INTEGER,
  photo_url TEXT NOT NULL,
  website TEXT,
  PRIMARY KEY (extension_id),
  CONSTRAINT fk_cruise_line_id FOREIGN KEY (cruise_line_id) REFERENCES antarctic.cruise_lines (cruise_line_id) ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE antarctic.expeditions_extensions (
  expedition_id INTEGER NOT NULL,
  extension_id INTEGER NOT NULL,
  CONSTRAINT fk_expedition_id FOREIGN KEY (expedition_id) REFERENCES antarctic.expeditions (expedition_id) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT fk_extension_id FOREIGN KEY (extension_id) REFERENCES antarctic.extensions (extension_id) ON DELETE CASCADE ON UPDATE NO ACTION
);

\i /postgresql/seed.sql

--DO $$
--BEGIN
--  INSERT INTO antarctic.cruise_lines (name, website, fleet_website, expedition_website, logo)
--  VALUES
--    (
--      'Ponant',
--      'https://us.ponant.com',
--      'https://us.ponant.com/cruises/themes/polar-expedition?pred-facet-destination%5B%5D=ANTARCTI'
--    ),
--    (
--      'Quark Expeditions',
--      'https://www.quarkexpeditions.com',
--      'https://www.quarkexpeditions.com/expeditions?f%5B0%5D=expedition_region%3Aantarctic'
--    ),
--    (
--      'Viking Expeditions',
--      'https://www.vikingcruises.com',
--      'https://www.vikingcruises.com/expeditions/search-cruises/index.html?Countries=Antarctica|Argentina|Falkland%20Islands|Georgia%20and%20the%20South%20Sandwich%20Islands'
--    );
--END $$;