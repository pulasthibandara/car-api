# --- !Ups

CREATE TABLE files(
  id UUID NOT NULL PRIMARY KEY,
  mime_type VARCHAR,
  provider VARCHAR NOT NULL,
  provider_id VARCHAR,
  slug VARCHAR,
  properties jsonb,
  business_id UUID NOT NULL,
  created_by UUID NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

# --- !Downs

DROP TABLE IF EXISTS files;
