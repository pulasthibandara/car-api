# --- !Ups

CREATE TABLE MAKES (
  ID VARCHAR(36) NOT NULL PRIMARY KEY,
  NAME VARCHAR NOT NULL
);

CREATE TABLE MODELS (
  ID VARCHAR(36) NOT NULL PRIMARY KEY,
  NAME VARCHAR NOT NULL,
  MAKE_ID VARCHAR(36) NOT NULL REFERENCES MAKES(ID)
);

CREATE TABLE LISTINGS (
  ID VARCHAR(36) NOT NULL PRIMARY KEY,
  MAKE VARCHAR(36) NOT NULL REFERENCES  MAKES(ID),
  MODEL VARCHAR(36) NOT NULL REFERENCES MODELS(ID),
  TITLE VARCHAR NOT NULL,
  DESCRIPTION VARCHAR
);

CREATE TABLE PHOTOS (
  ID VARCHAR(36) NOT NULL PRIMARY KEY,
  NAME VARCHAR NOT NULL,
  USER_ID VARCHAR(36) NOT NULL REFERENCES USERS(ID),
  LISTING_ID  VARCHAR(36) REFERENCES LISTINGS(ID)
);

INSERT INTO MAKES VALUES (
  "d5d0d6aa-3adb-11e8-b467-0ed5f89f718b", "Toyota",
  "d5d0d93e-3adb-11e8-b467-0ed5f89f718b", "Nissan",
  "d5d0dce0-3adb-11e8-b467-0ed5f89f718b", "Mazda",
  "d5d0de34-3adb-11e8-b467-0ed5f89f718b", "Honda",
  "d5d0df60-3adb-11e8-b467-0ed5f89f718b", "Hyundai",
  "d5d0e082-3adb-11e8-b467-0ed5f89f718b", "BMW",
  "d5d0e19a-3adb-11e8-b467-0ed5f89f718b", "Volkswagen ",
  "d5d0e2bc-3adb-11e8-b467-0ed5f89f718b", "Suzuki",
  "d5d0e640-3adb-11e8-b467-0ed5f89f718b", "Mercedes-Benz",
  "d5d0e78a-3adb-11e8-b467-0ed5f89f718b", "Mitsubishi"
);


INSERT INTO MODELS VALUES (
  "82b2a276-3add-11e8-b467-0ed5f89f718b", "Avalon", "d5d0d6aa-3adb-11e8-b467-0ed5f89f718b",
  "82b2a51e-3add-11e8-b467-0ed5f89f718b", "Camry", "d5d0d6aa-3adb-11e8-b467-0ed5f89f718b",
  "82b2a668-3add-11e8-b467-0ed5f89f718b", "Corolla", "d5d0d6aa-3adb-11e8-b467-0ed5f89f718b",
  "82b2aa00-3add-11e8-b467-0ed5f89f718b", "Yaris", "d5d0d6aa-3adb-11e8-b467-0ed5f89f718b",
  "82b2ab5e-3add-11e8-b467-0ed5f89f718b", "Prius", "d5d0d6aa-3adb-11e8-b467-0ed5f89f718b",
  "82b2ac80-3add-11e8-b467-0ed5f89f718b", "Camry Hybrid", "d5d0d6aa-3adb-11e8-b467-0ed5f89f718b",
  "82b2ada2-3add-11e8-b467-0ed5f89f718b", "Crown", "d5d0d6aa-3adb-11e8-b467-0ed5f89f718b",
  "82b2aec4-3add-11e8-b467-0ed5f89f718b", "Mark X", "d5d0d6aa-3adb-11e8-b467-0ed5f89f718b",
  "82b2b090-3add-11e8-b467-0ed5f89f718b", "Allion", "d5d0d6aa-3adb-11e8-b467-0ed5f89f718b",
  "82b2b1ee-3add-11e8-b467-0ed5f89f718b", "Premio", "d5d0d6aa-3adb-11e8-b467-0ed5f89f718b",
  "82b2b716-3add-11e8-b467-0ed5f89f718b", "Micra", "d5d0d93e-3adb-11e8-b467-0ed5f89f718b",
  "82b2b892-3add-11e8-b467-0ed5f89f718b", "Sentra", "d5d0d93e-3adb-11e8-b467-0ed5f89f718b",
  "82b2b9dc-3add-11e8-b467-0ed5f89f718b", "Qashqai", "d5d0d93e-3adb-11e8-b467-0ed5f89f718b",
  "82b2bb08-3add-11e8-b467-0ed5f89f718b", "Juke", "d5d0d93e-3adb-11e8-b467-0ed5f89f718b",
  "82b2bc2a-3add-11e8-b467-0ed5f89f718b", "Frontier", "d5d0d93e-3adb-11e8-b467-0ed5f89f718b",
  "82b2bd4c-3add-11e8-b467-0ed5f89f718b", "Rouge", "d5d0d93e-3adb-11e8-b467-0ed5f89f718b",
  "82b2be6e-3add-11e8-b467-0ed5f89f718b", "Altima", "d5d0d93e-3adb-11e8-b467-0ed5f89f718b",
  "82b2c1a2-3add-11e8-b467-0ed5f89f718b", "Demio","d5d0dce0-3adb-11e8-b467-0ed5f89f718b",
  "82b2c2f6-3add-11e8-b467-0ed5f89f718b", "Mazda 5","d5d0dce0-3adb-11e8-b467-0ed5f89f718b",
  "82b2c422-3add-11e8-b467-0ed5f89f718b", "Mazda 6","d5d0dce0-3adb-11e8-b467-0ed5f89f718b",
  "82b2c54e-3add-11e8-b467-0ed5f89f718b", "CX-3","d5d0dce0-3adb-11e8-b467-0ed5f89f718b",
  "82b2c67a-3add-11e8-b467-0ed5f89f718b", "Accord", "d5d0de34-3adb-11e8-b467-0ed5f89f718b",
  "82b2c79c-3add-11e8-b467-0ed5f89f718b", "Amaze", "d5d0de34-3adb-11e8-b467-0ed5f89f718b",
  "82b2c8be-3add-11e8-b467-0ed5f89f718b", "Avancier", "d5d0de34-3adb-11e8-b467-0ed5f89f718b",
  "82b2cc24-3add-11e8-b467-0ed5f89f718b", "Brio", "d5d0de34-3adb-11e8-b467-0ed5f89f718b",
  "82b2cd96-3add-11e8-b467-0ed5f89f718b", "BR-V", "d5d0de34-3adb-11e8-b467-0ed5f89f718b",
  "82b2cee0-3add-11e8-b467-0ed5f89f718b", "City", "d5d0de34-3adb-11e8-b467-0ed5f89f718b",
  "82b2d002-3add-11e8-b467-0ed5f89f718b", "320", "d5d0e082-3adb-11e8-b467-0ed5f89f718b",
  "82b2d124-3add-11e8-b467-0ed5f89f718b", "M3", "d5d0e082-3adb-11e8-b467-0ed5f89f718b",
  "82b2d250-3add-11e8-b467-0ed5f89f718b", "Golf", "d5d0e19a-3adb-11e8-b467-0ed5f89f718b"
);

# --- !Downs

DROP TABLE MAKES;
DROP TABLE MODELS;
