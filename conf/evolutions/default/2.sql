# --- !Ups

CREATE TABLE MAKES (
  ID UUID NOT NULL PRIMARY KEY,
  NAME VARCHAR NOT NULL,
  SLUG VARCHAR NOT NULL UNIQUE,
  CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE MODELS (
  ID UUID NOT NULL PRIMARY KEY,
  NAME VARCHAR NOT NULL,
  MAKE_ID UUID NOT NULL REFERENCES MAKES(ID),
  SLUG VARCHAR NOT NULL UNIQUE,
  CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE USER_MODELS (
  MODEL_ID UUID NOT NULL REFERENCES MODELS(ID),
  USER_ID UUID NOT NULL,
  CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY(MODEL_ID, USER_ID)
);

CREATE TABLE LISTINGS (
  ID UUID NOT NULL PRIMARY KEY,
  MAKE_ID UUID NOT NULL REFERENCES  MAKES(ID),
  MODEL_ID UUID NOT NULL REFERENCES MODELS(ID),
  USER_ID UUID NOT NULL,
  TITLE VARCHAR NOT NULL,
  SLUG VARCHAR NOT NULL,
  DESCRIPTION VARCHAR,
  YEAR INT,
  KILOMETERS BIGINT,
  COLOR VARCHAR,
  BODY_TYPE VARCHAR,
  FUEL_TYPE VARCHAR,
  TRANSMISSION_TYPE VARCHAR,
  CYLINDERS VARCHAR,
  ENGINE_SIZE INT,
  CONDITION_TYPE VARCHAR,
  FEATURES VARCHAR[] NOT NULL,
  CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX listings_slug_idx ON listings(USER_ID, SLUG);

CREATE TABLE PHOTOS (
  ID UUID NOT NULL PRIMARY KEY,
  NAME VARCHAR NOT NULL,
  USER_ID UUID NOT NULL,
  LISTING_ID  UUID REFERENCES LISTINGS(ID),
  CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO MAKES(ID, NAME, SLUG) VALUES
  ('d5d0d6aa-3adb-11e8-b467-0ed5f89f718b', 'Toyota', 'toyota'),
  ('d5d0d93e-3adb-11e8-b467-0ed5f89f718b', 'Nissan', 'nissan'),
  ('d5d0dce0-3adb-11e8-b467-0ed5f89f718b', 'Mazda', 'mazda'),
  ('d5d0de34-3adb-11e8-b467-0ed5f89f718b', 'Honda', 'honda'),
  ('d5d0df60-3adb-11e8-b467-0ed5f89f718b', 'Hyundai', 'hyundai'),
  ('d5d0e082-3adb-11e8-b467-0ed5f89f718b', 'BMW', 'bmw'),
  ('d5d0e19a-3adb-11e8-b467-0ed5f89f718b', 'Volkswagen ', 'volkswagen'),
  ('d5d0e2bc-3adb-11e8-b467-0ed5f89f718b', 'Suzuki', 'suzuki'),
  ('d5d0e640-3adb-11e8-b467-0ed5f89f718b', 'Mercedes-Benz', 'mercedes-benz'),
  ('d5d0e78a-3adb-11e8-b467-0ed5f89f718b', 'Mitsubishi', 'mitsubishi');

INSERT INTO MODELS(ID, NAME, MAKE_ID, SLUG) VALUES
  ('82b2a276-3add-11e8-b467-0ed5f89f718b', 'Avalon', 'd5d0d6aa-3adb-11e8-b467-0ed5f89f718b', 'avalon'),
  ('82b2a51e-3add-11e8-b467-0ed5f89f718b', 'Camry', 'd5d0d6aa-3adb-11e8-b467-0ed5f89f718b', 'camry'),
  ('82b2a668-3add-11e8-b467-0ed5f89f718b', 'Corolla', 'd5d0d6aa-3adb-11e8-b467-0ed5f89f718b', 'corolla'),
  ('82b2aa00-3add-11e8-b467-0ed5f89f718b', 'Yaris', 'd5d0d6aa-3adb-11e8-b467-0ed5f89f718b', 'yaris'),
  ('82b2ab5e-3add-11e8-b467-0ed5f89f718b', 'Prius', 'd5d0d6aa-3adb-11e8-b467-0ed5f89f718b', 'prius'),
  ('82b2ac80-3add-11e8-b467-0ed5f89f718b', 'Camry Hybrid', 'd5d0d6aa-3adb-11e8-b467-0ed5f89f718b', 'camry-hybrid'),
  ('82b2ada2-3add-11e8-b467-0ed5f89f718b', 'Crown', 'd5d0d6aa-3adb-11e8-b467-0ed5f89f718b', 'crown'),
  ('82b2aec4-3add-11e8-b467-0ed5f89f718b', 'Mark X', 'd5d0d6aa-3adb-11e8-b467-0ed5f89f718b', 'mark-x'),
  ('82b2b090-3add-11e8-b467-0ed5f89f718b', 'Allion', 'd5d0d6aa-3adb-11e8-b467-0ed5f89f718b', 'allion'),
  ('82b2b1ee-3add-11e8-b467-0ed5f89f718b', 'Premio', 'd5d0d6aa-3adb-11e8-b467-0ed5f89f718b', 'premio'),
  ('82b2b716-3add-11e8-b467-0ed5f89f718b', 'Micra', 'd5d0d93e-3adb-11e8-b467-0ed5f89f718b', 'micra'),
  ('82b2b892-3add-11e8-b467-0ed5f89f718b', 'Sentra', 'd5d0d93e-3adb-11e8-b467-0ed5f89f718b', 'sentra'),
  ('82b2b9dc-3add-11e8-b467-0ed5f89f718b', 'Qashqai', 'd5d0d93e-3adb-11e8-b467-0ed5f89f718b', 'qashqai'),
  ('82b2bb08-3add-11e8-b467-0ed5f89f718b', 'Juke', 'd5d0d93e-3adb-11e8-b467-0ed5f89f718b', 'juke'),
  ('82b2bc2a-3add-11e8-b467-0ed5f89f718b', 'Frontier', 'd5d0d93e-3adb-11e8-b467-0ed5f89f718b', 'frontier'),
  ('82b2bd4c-3add-11e8-b467-0ed5f89f718b', 'Rouge', 'd5d0d93e-3adb-11e8-b467-0ed5f89f718b', 'rouge'),
  ('82b2be6e-3add-11e8-b467-0ed5f89f718b', 'Altima', 'd5d0d93e-3adb-11e8-b467-0ed5f89f718b', 'altima'),
  ('82b2c1a2-3add-11e8-b467-0ed5f89f718b', 'Demio','d5d0dce0-3adb-11e8-b467-0ed5f89f718b', 'demio'),
  ('82b2c2f6-3add-11e8-b467-0ed5f89f718b', 'Mazda 5', 'd5d0dce0-3adb-11e8-b467-0ed5f89f718b', 'mazda-5'),
  ('82b2c422-3add-11e8-b467-0ed5f89f718b', 'Mazda 6', 'd5d0dce0-3adb-11e8-b467-0ed5f89f718b', 'mazda-6'),
  ('82b2c54e-3add-11e8-b467-0ed5f89f718b', 'CX-3', 'd5d0dce0-3adb-11e8-b467-0ed5f89f718b', 'cx-3'),
  ('82b2c67a-3add-11e8-b467-0ed5f89f718b', 'Accord', 'd5d0de34-3adb-11e8-b467-0ed5f89f718b', 'accord'),
  ('82b2c79c-3add-11e8-b467-0ed5f89f718b', 'Amaze', 'd5d0de34-3adb-11e8-b467-0ed5f89f718b', 'amaze'),
  ('82b2c8be-3add-11e8-b467-0ed5f89f718b', 'Avancier', 'd5d0de34-3adb-11e8-b467-0ed5f89f718b', 'avancier'),
  ('82b2cc24-3add-11e8-b467-0ed5f89f718b', 'Brio', 'd5d0de34-3adb-11e8-b467-0ed5f89f718b', 'brio'),
  ('82b2cd96-3add-11e8-b467-0ed5f89f718b', 'BR-V', 'd5d0de34-3adb-11e8-b467-0ed5f89f718b', 'br-v'),
  ('82b2cee0-3add-11e8-b467-0ed5f89f718b', 'City', 'd5d0de34-3adb-11e8-b467-0ed5f89f718b', 'city'),
  ('82b2d002-3add-11e8-b467-0ed5f89f718b', '320', 'd5d0e082-3adb-11e8-b467-0ed5f89f718b', '320'),
  ('82b2d124-3add-11e8-b467-0ed5f89f718b', 'M3', 'd5d0e082-3adb-11e8-b467-0ed5f89f718b', 'm3'),
  ('82b2d250-3add-11e8-b467-0ed5f89f718b', 'Golf', 'd5d0e19a-3adb-11e8-b467-0ed5f89f718b', 'golf');

# --- !Downs

DROP TABLE IF EXISTS PHOTOS;
DROP TABLE IF EXISTS LISTINGS;
DROP TABLE IF EXISTS USER_MODELS;
DROP TABLE IF EXISTS MODELS;
DROP TABLE IF EXISTS MAKES;
