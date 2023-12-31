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
  name VARCHAR(50) NOT NULL,
  website VARCHAR(255) NOT NULL,
  PRIMARY KEY (cruise_line_id)
);

CREATE TABLE antarctica.expeditions (
  expedition_id SERIAL,
  cruise_line_id INTEGER NOT NULL,
  website VARCHAR(255) NOT NULL,
  title VARCHAR(255) NOT NULL,
  description TEXT NOT NULL,
  starting_price DECIMAL(10, 4) NOT NULL,
  num_days INTEGER NOT NULL,
  num_countries INTEGER NOT NULL,
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
  websites VARCHAR[] := ARRAY['https://world.expeditions.com/', 'https://www.quarkexpeditions.com/', 'https://www.vikingcruises.com/expeditions', 'https://www.aurora-expeditions.com/destination/', 'https://www.seabourn.com/en/cruise-destinations/expedition', 'https://us.ponant.com/', 'https://www.hurtigruten.com/en-us/expeditions/'];
  i INTEGER;
BEGIN
  FOR i IN 1..array_length(cruise_lines, 1) LOOP
    INSERT INTO antarctica.cruise_lines (name, website)
    VALUES (cruise_lines[i], websites[i]);
  END LOOP;
END $$;
