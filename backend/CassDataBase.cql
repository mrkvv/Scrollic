CREATE KEYSPACE IF NOT EXISTS scrollic
WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};


USE scrollic;

CREATE TABLE IF NOT EXISTS news (
    id UUID PRIMARY KEY,
    head TEXT,
    summary TEXT,
    text TEXT,
    url TEXT,
    url_picture TEXT,
    popularity INT,
    theme_id INT,
    created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS news_by_theme_and_popularity (
    theme_id INT,
    popularity INT,
    created_at TIMESTAMP,
    id UUID,
    head TEXT,
    summary TEXT,
    text TEXT,
    url TEXT,
    url_picture TEXT,
    PRIMARY KEY (theme_id, popularity, created_at, id)
) WITH CLUSTERING ORDER BY (popularity DESC, created_at DESC, id ASC);

CREATE TABLE IF NOT EXISTS news_by_date (
    date_bucket TEXT,
    created_at TIMESTAMP,
    id UUID,
    theme_id INT,
    popularity INT,
    head TEXT,
    summary TEXT,
    url TEXT,
    PRIMARY KEY (date_bucket, created_at, id)
) WITH CLUSTERING ORDER BY (created_at DESC, id ASC);

CREATE TABLE IF NOT EXISTS user_likes (
    user_id INT,
    news_id UUID,
    liked_at TIMESTAMP,
    PRIMARY KEY (user_id, news_id)
);

CREATE TABLE IF NOT EXISTS user_seen (
    user_id INT,
    news_id UUID,
    seen_at TIMESTAMP,
    PRIMARY KEY (user_id, news_id)
) WITH default_time_to_live = 604800;

CREATE TABLE IF NOT EXISTS user_theme_weights (
    user_id INT,
    theme_id INT,
    weight DECIMAL,
    updated_at TIMESTAMP,
    PRIMARY KEY (user_id, theme_id)
);

CREATE TABLE IF NOT EXISTS news_popularity (
    news_id UUID PRIMARY KEY,
    likes_count COUNTER
);