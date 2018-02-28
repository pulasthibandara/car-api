# --- !Ups

CREATE TABLE USERS (
  ID varchar(36) NOT NULL,
  EMAIL varchar NOT NULL,
  FIRST_NAME varchar NOT NULL,
  LAST_NAME varchar NOT NULL,
  PASSWORD varchar NOT NULL,
  CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (ID)
);

INSERT INTO USERS VALUES ('1 id', 'mario', 'last', 'mario@example.com', 's3cr3t', DEFAULT);
INSERT INTO USERS VALUES ('2 id', 'Fred', 'last', 'fred@flinstones.com', 'wilmalove', DEFAULT);

# --- !Downs

DROP TABLE USERS;