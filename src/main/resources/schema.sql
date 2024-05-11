
CREATE TABLE IF NOT EXISTS event_store
(
  id bigint NOT NULL,
  stream_id text NOT NULL,
  version integer NOT NULL,

  event_type text NOT NULL,
  event_type_version integer NOT NULL,
  data jsonb NOT NULL,

  meta jsonb NOT NULL,

  CONSTRAINT event_store_pkey PRIMARY KEY (id),
  UNIQUE (stream_id, version)
);