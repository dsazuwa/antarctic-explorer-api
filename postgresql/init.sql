ALTER TABLE IF EXISTS antarctica.expeditions_extensions DROP CONSTRAINT IF EXISTS fk_extension_id;
ALTER TABLE IF EXISTS antarctica.expeditions_extensions DROP CONSTRAINT IF EXISTS fk_expedition_id;
ALTER TABLE IF EXISTS antarctica.extensions DROP CONSTRAINT IF EXISTS fk_cruise_line_id;
ALTER TABLE IF EXISTS antarctica.departures DROP CONSTRAINT IF EXISTS fk_itinerary_id;
ALTER TABLE IF EXISTS antarctica.departures DROP CONSTRAINT IF EXISTS fk_vessel_id;
ALTER TABLE IF EXISTS antarctica.departures DROP CONSTRAINT IF EXISTS fk_expedition_id;
ALTER TABLE IF EXISTS antarctica.itineraries DROP CONSTRAINT IF EXISTS fk_expedition_id;
ALTER TABLE IF EXISTS antarctica.vessels DROP CONSTRAINT IF EXISTS fk_cruise_line_id;
ALTER TABLE IF EXISTS antarctica.gallery DROP CONSTRAINT IF EXISTS fk_expedition;
ALTER TABLE IF EXISTS antarctica.expeditions DROP CONSTRAINT IF EXISTS fk_cruise_line_id;

DROP TABLE IF EXISTS antarctica.expeditions_extensions;
DROP TABLE IF EXISTS antarctica.extensions;
DROP TABLE IF EXISTS antarctica.departures;
DROP TABLE IF EXISTS antarctica.itinerary_details;
DROP TABLE IF EXISTS antarctica.itineraries;
DROP TABLE IF EXISTS antarctica.vessels;
DROP TABLE IF EXISTS antarctica.gallery;
DROP TABLE IF EXISTS antarctica.expeditions;
DROP TABLE IF EXISTS antarctica.cruise_lines;

DROP SCHEMA IF EXISTS antarctica;

CREATE SCHEMA antarctica;

CREATE TABLE antarctica.cruise_lines (
  cruise_line_id SERIAL,
  name VARCHAR(50) NOT NULL UNIQUE,
  website VARCHAR(255) NOT NULL UNIQUE,
  fleet_website VARCHAR(255) UNIQUE,
  expedition_website TEXT NOT NULL UNIQUE,
  logo TEXT NOT NULL UNIQUE,
  PRIMARY KEY (cruise_line_id)
);

CREATE TABLE antarctica.expeditions (
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
  CONSTRAINT fk_cruise_line_id FOREIGN KEY (cruise_line_id) REFERENCES antarctica.cruise_lines (cruise_line_id) ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE antarctica.gallery (
  photo_id SERIAL,
  expedition_id INTEGER NOT NULL,
  photo_url TEXT NOT NULL,
  alt TEXT,
  PRIMARY KEY (photo_id),
  CONSTRAINT fk_expedition_id FOREIGN KEY (expedition_id) REFERENCES antarctica.expeditions (expedition_id) ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE antarctica.vessels (
  vessel_id SERIAL,
  cruise_line_id INTEGER NOT NULL,
  name VARCHAR NOT NULL,
  capacity INTEGER NOT NULL,
  cabins INTEGER,
  description TEXT[],
  website TEXT,
  photo_url TEXT NOT NULL,
  PRIMARY KEY (vessel_id),
  CONSTRAINT fk_cruise_line_id FOREIGN KEY (cruise_line_id) REFERENCES antarctica.cruise_lines (cruise_line_id) ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE antarctica.itineraries (
  itinerary_id SERIAL,
  expedition_id INTEGER NOT NULL,
  name VARCHAR(255),
  departing_from VARCHAR(100),
  arriving_at VARCHAR(100),
  duration VARCHAR(10) NOT NULL,
  map_url TEXT,
  PRIMARY KEY (itinerary_id),
  CONSTRAINT fk_expedition_id FOREIGN KEY (expedition_id) REFERENCES antarctica.expeditions (expedition_id) ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE antarctica.itinerary_details (
  detail_id SERIAL,
  itinerary_id INTEGER NOT NULL,
  day VARCHAR(10) NOT NULL,
  header VARCHAR(255) NOT NULL,
  content TEXT[] NOT NULL,
  PRIMARY KEY (detail_id),
  CONSTRAINT fk_itinerary_id FOREIGN KEY (itinerary_id) REFERENCES antarctica.itineraries (itinerary_id) ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE antarctica.departures (
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
  CONSTRAINT fk_expedition_id FOREIGN KEY (expedition_id) REFERENCES antarctica.expeditions (expedition_id) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT fk_vessel_id FOREIGN KEY (vessel_id) REFERENCES antarctica.vessels (vessel_id) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT fk_itinerary_id FOREIGN KEY (itinerary_id) REFERENCES antarctica.itineraries (itinerary_id) ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE antarctica.extensions (
  extension_id SERIAL,
  cruise_line_id INTEGER NOT NULL,
  name VARCHAR NOT NULL, 
  starting_price DECIMAL(10, 4),
  duration INTEGER,
  photo_url TEXT NOT NULL,
  website TEXT,
  PRIMARY KEY (extension_id),
  CONSTRAINT fk_cruise_line_id FOREIGN KEY (cruise_line_id) REFERENCES antarctica.cruise_lines (cruise_line_id) ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE antarctica.expeditions_extensions (
  expedition_id INTEGER NOT NULL,
  extension_id INTEGER NOT NULL,
  CONSTRAINT fk_expedition_id FOREIGN KEY (expedition_id) REFERENCES antarctica.expeditions (expedition_id) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT fk_extension_id FOREIGN KEY (extension_id) REFERENCES antarctica.extensions (extension_id) ON DELETE CASCADE ON UPDATE NO ACTION
);

DO $$
BEGIN
  INSERT INTO antarctica.cruise_lines (name, website, fleet_website, expedition_website, logo)
  VALUES
    (
      'Aurora Expeditions',
      'https://www.aurora-expeditions.com/destination',
      NULL,
      'https://www.aurora-expeditions.com/find-an-expedition/?search&destinations%5B0%5D=antarctica-cruises&destinations%5B1%5D=antarctic-peninsula&destinations%5B2%5D=weddell-sea&destinations%5B3%5D=south-georgia-island&destinations%5B4%5D=falkland-islands-malvinas&destinations%5B5%5D=antarctic-circle&destinations%5B6%5D=patagonia&departDates&voyage_types%5B0%5D=expedition',
      'https://res.cloudinary.com/dcdakh7gh/image/upload/v1710007648/antarctica-explorer/AuroraLogo.png'
    ),
    (
      'Hurtigruten Expeditions',
      'https://www.hurtigruten.com',
      'https://www.hurtigruten.com/en-us/expeditions/ships/',
      'https://www.hurtigruten.com/en-us/expeditions/cruises/?forceRefresh=true&destinations=antarctica-cruises',
      'https://res.cloudinary.com/dcdakh7gh/image/upload/v1710007537/antarctica-explorer/HurtigrutenLogo.png'
    ),
    (
      'Lindblad Expeditions',
      'https://world.expeditions.com',
      'https://world.expeditions.com/about/fleet#ships',
      'https://world.expeditions.com/book?destinations.name=Antarctica&destinations.name=Patagonia',
      'https://res.cloudinary.com/dcdakh7gh/image/upload/v1710007542/antarctica-explorer/LindbladLogo.png'
    );
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
--      'Seaborn Expeditions',
--      'https://www.seabourn.com/en/cruise-destinations/expedition',
--      'https://www.seabourn.com/en/find-a-cruise?destinationIds:(S)'
--    ),
--    (
--      'Viking Expeditions',
--      'https://www.vikingcruises.com',
--      'https://www.vikingcruises.com/expeditions/search-cruises/index.html?Countries=Antarctica|Argentina|Falkland%20Islands|Georgia%20and%20the%20South%20Sandwich%20Islands'
--    );
END $$;