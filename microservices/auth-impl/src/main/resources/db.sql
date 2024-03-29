CREATE TABLE IF NOT EXISTS auth_users
(
    id            SERIAL PRIMARY KEY,
    email         CHARACTER VARYING(255) UNIQUE,
    password      CHARACTER VARYING(255),
    is_verified   BOOLEAN DEFAULT false NOT NULL,
    created_at    TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    phone         CHARACTER VARYING(50) UNIQUE,
    "role"        CHARACTER VARYING(15) NOT NULL
);

CREATE TABLE IF NOT EXISTS sms_codes
(
    phone        CHARACTER VARYING(50) NOT NULL,
    code         CHARACTER VARYING(10) NOT NULL,
    created_at   TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);
