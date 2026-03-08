CREATE TABLE IF NOT EXISTS theme (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    keys JSONB
);

CREATE TABLE IF NOT EXISTS news (
    id SERIAL PRIMARY KEY,
    head TEXT NOT NULL,
    sum TEXT,
    text TEXT,
    url TEXT UNIQUE,
    url_picture TEXT,
    popularity INTEGER DEFAULT 0,
    theme_id INTEGER REFERENCES theme(id) ON DELETE SET NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    weights JSONB DEFAULT '{}'::jsonb,
    seen JSONB DEFAULT '[]'::jsonb,
    likes JSONB DEFAULT '[]'::jsonb
);
