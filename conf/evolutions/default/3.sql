# --- !Ups

CREATE TABLE businesses(
  id UUID NOT NULL PRIMARY KEY,
  name VARCHAR NOT NULL,
  subdomain VARCHAR UNIQUE,
  domain VARCHAR UNIQUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

# --- !Downs

DROP TABLE IF EXISTS businesses;
