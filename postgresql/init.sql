ALTER TABLE IF EXISTS antarctica.expeditions_vessels DROP CONSTRAINT fk_departure_id;
ALTER TABLE IF EXISTS antarctica.expeditions_vessels DROP CONSTRAINT fk_vessel_id;
ALTER TABLE IF EXISTS antarctica.vessels DROP CONSTRAINT fk_cruise_line_id;
ALTER TABLE IF EXISTS antarctica.activities DROP CONSTRAINT fk_expedition_id;
ALTER TABLE IF EXISTS antarctica.departures DROP CONSTRAINT fk_expedition_id;
ALTER TABLE IF EXISTS antarctica.itineraries DROP CONSTRAINT fk_expedition_id;
ALTER TABLE IF EXISTS antarctica.expeditions DROP CONSTRAINT fk_cruise_line_id;

DROP TABLE IF EXISTS antarctica.expeditions_vessels;
DROP TABLE IF EXISTS antarctica.vessels;
DROP TABLE IF EXISTS antarctica.activities;
DROP TABLE IF EXISTS antarctica.departures;
DROP TABLE IF EXISTS antarctica.itineraries;
DROP TABLE IF EXISTS antarctica.expeditions;
DROP TABLE IF EXISTS antarctica.cruise_lines;

DROP SCHEMA IF EXISTS antarctica;

CREATE SCHEMA antarctica;

CREATE TABLE antarctica.cruise_lines (
  cruise_line_id SERIAL,
  name VARCHAR(50) NOT NULL UNIQUE,
  website VARCHAR(255) NOT NULL UNIQUE,
  expedition_website TEXT NOT NULL UNIQUE,
  logo TEXT NOT NULL UNIQUE,
  PRIMARY KEY (cruise_line_id)
);

CREATE TABLE antarctica.expeditions (
  expedition_id SERIAL,
  cruise_line_id INTEGER NOT NULL,
  website TEXT,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  highlights TEXT[],
  departing_from VARCHAR(100),
  arriving_at VARCHAR(100),
  duration VARCHAR(50) NOT NULL,
  starting_price DECIMAL(10, 4),
  photo_url TEXT,
  PRIMARY KEY (expedition_id),
  CONSTRAINT fk_cruise_line_id FOREIGN KEY (cruise_line_id) REFERENCES antarctica.cruise_lines (cruise_line_id) ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE antarctica.itineraries (
  itinerary_id SERIAL,
  expedition_id INTEGER NOT NULL,
  day VARCHAR(10) NOT NULL,
  header VARCHAR(255) NOT NULL,
  content TEXT NOT NULL,
  PRIMARY KEY (itinerary_id),
  CONSTRAINT fk_expedition_id FOREIGN KEY (expedition_id) REFERENCES antarctica.expeditions (expedition_id) ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE antarctica.departures (
  departure_id SERIAL,
  expedition_id INTEGER NOT NULL,
  name VARCHAR,
  departing_from VARCHAR(100),
  arriving_at VARCHAR(100),
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  starting_price DECIMAL(10, 4),
  website TEXT,
  PRIMARY KEY (departure_id),
  CONSTRAINT fk_expedition_id FOREIGN KEY (expedition_id) REFERENCES antarctica.expeditions (expedition_id) ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE antarctica.activities (
  activity_id SERIAL,
  expedition_id INTEGER NOT NULL,
  name VARCHAR(150) NOT NULL,
  description TEXT,
  is_included BOOLEAN NOT NULL,
  price DECIMAL(10, 4),
  PRIMARY KEY (activity_id),
  CONSTRAINT fk_expedition_id FOREIGN KEY (expedition_id) REFERENCES antarctica.expeditions (expedition_id) ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE antarctica.vessels (
  vessel_id SERIAL,
  cruise_line_id INTEGER NOT NULL,
  name VARCHAR NOT NULL,
  description TEXT,
  capacity INTEGER NOT NULL,
  year_built VARCHAR(10),
  year_refurbished VARCHAR(10),
  website TEXT,
  PRIMARY KEY (vessel_id),
  CONSTRAINT fk_cruise_line_id FOREIGN KEY (cruise_line_id) REFERENCES antarctica.cruise_lines (cruise_line_id) ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE antarctica.expeditions_vessels (
  vessel_id INTEGER NOT NULL,
  departure_id INTEGER NOT NULL,
  CONSTRAINT fk_vessel_id FOREIGN KEY (vessel_id) REFERENCES antarctica.vessels (vessel_id) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT fk_departure_id FOREIGN KEY (departure_id) REFERENCES antarctica.departures (departure_id) ON DELETE CASCADE ON UPDATE NO ACTION
);
