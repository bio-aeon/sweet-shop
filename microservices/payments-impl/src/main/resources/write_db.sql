CREATE TABLE IF NOT EXISTS card_link_event
(
  id BIGSERIAL NOT NULL,
  key TEXT NOT NULL,
  seq_nr INTEGER NOT NULL
    CONSTRAINT card_link_event_seq_nr_check
      CHECK (seq_nr > 0),
  type_hint TEXT NOT NULL,
  bytes BYTEA NOT NULL,
  tags TEXT[] NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS card_link_event_id_uindex
  ON card_link_event (id);

CREATE UNIQUE INDEX IF NOT EXISTS card_link_event_key_seq_nr_uindex
  ON card_link_event (key, seq_nr);

CREATE INDEX IF NOT EXISTS card_link_event_tags ON card_link_event (tags);

CREATE TABLE IF NOT EXISTS customer_event
(
  id BIGSERIAL NOT NULL,
  key TEXT NOT NULL,
  seq_nr INTEGER NOT NULL
    CONSTRAINT customer_event_seq_nr_check
      CHECK (seq_nr > 0),
  type_hint TEXT NOT NULL,
  bytes BYTEA NOT NULL,
  tags TEXT[] NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS customer_event_id_uindex
  ON customer_event (id);

CREATE UNIQUE INDEX IF NOT EXISTS customer_event_key_seq_nr_uindex
  ON customer_event (key, seq_nr);

CREATE INDEX IF NOT EXISTS customer_event_tags ON customer_event (tags);

CREATE TABLE IF NOT EXISTS payment_event
(
  id BIGSERIAL NOT NULL,
  key TEXT NOT NULL,
  seq_nr INTEGER NOT NULL
    CONSTRAINT payment_event_seq_nr_check
      CHECK (seq_nr > 0),
  type_hint TEXT NOT NULL,
  bytes BYTEA NOT NULL,
  tags TEXT[] NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS payment_event_id_uindex
  ON payment_event (id);

CREATE UNIQUE INDEX IF NOT EXISTS payment_event_key_seq_nr_uindex
  ON payment_event (key, seq_nr);

CREATE INDEX IF NOT EXISTS payment_event_tags ON payment_event (tags);
