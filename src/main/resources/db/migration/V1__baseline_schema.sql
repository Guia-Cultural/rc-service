-- V1__baseline_schema.sql
-- Esquema base das tabelas principais do Rota Cultural (referência rc-db / JPA)

CREATE TABLE IF NOT EXISTS users (
    id_user BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL,
    password VARCHAR(255) NOT NULL,
    cep VARCHAR(11),
    role ENUM('ADMIN', 'NATIVO', 'USUARIO') NOT NULL,
    bio VARCHAR(500),
    date_create DATETIME(6) NOT NULL,
    PRIMARY KEY (id_user),
    UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS categories (
    id_cat BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(75) NOT NULL,
    PRIMARY KEY (id_cat)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS places (
    id_loc BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(500) NOT NULL,
    id_cat BIGINT,
    address VARCHAR(150) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(150) NOT NULL,
    country VARCHAR(100) NOT NULL,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    business_hours VARCHAR(150) NOT NULL,
    price ENUM('GRATUITO', 'BARATO', 'MODERADO', 'CARO'),
    create_by BIGINT NOT NULL,
    status ENUM('APROVADO', 'REJEITADO', 'EM_AVALIACAO'),
    date_create DATETIME(6) NOT NULL,
    PRIMARY KEY (id_loc),
    CONSTRAINT fk_places_category FOREIGN KEY (id_cat) REFERENCES categories (id_cat),
    CONSTRAINT fk_places_creator FOREIGN KEY (create_by) REFERENCES users (id_user)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS trips (
    id_trip BIGINT NOT NULL AUTO_INCREMENT,
    id_user BIGINT NOT NULL,
    destination VARCHAR(150) NOT NULL,
    start_date DATE,
    end_date DATE,
    budget DECIMAL(10, 2),
    date_create DATETIME(6) NOT NULL,
    PRIMARY KEY (id_trip),
    CONSTRAINT fk_trips_user FOREIGN KEY (id_user) REFERENCES users (id_user)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS preferences (
    id_pref BIGINT NOT NULL AUTO_INCREMENT,
    id_trip BIGINT NOT NULL,
    id_cat BIGINT NOT NULL,
    PRIMARY KEY (id_pref),
    UNIQUE KEY uk_preferences_trip_cat (id_trip, id_cat),
    CONSTRAINT fk_preferences_trip FOREIGN KEY (id_trip) REFERENCES trips (id_trip),
    CONSTRAINT fk_preferences_category FOREIGN KEY (id_cat) REFERENCES categories (id_cat)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS itinerary (
    id_itinerario BIGINT NOT NULL AUTO_INCREMENT,
    id_trip BIGINT NOT NULL,
    day DATE NOT NULL,
    id_loc BIGINT NOT NULL,
    description VARCHAR(500),
    PRIMARY KEY (id_itinerario),
    CONSTRAINT fk_itinerary_trip FOREIGN KEY (id_trip) REFERENCES trips (id_trip),
    CONSTRAINT fk_itinerary_place FOREIGN KEY (id_loc) REFERENCES places (id_loc)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS favorites (
    id_user BIGINT NOT NULL,
    id_loc BIGINT NOT NULL,
    date_create DATETIME(6) NOT NULL,
    PRIMARY KEY (id_user, id_loc),
    CONSTRAINT fk_favorites_user FOREIGN KEY (id_user) REFERENCES users (id_user),
    CONSTRAINT fk_favorites_place FOREIGN KEY (id_loc) REFERENCES places (id_loc)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS feedbacks (
    id_feed BIGINT NOT NULL AUTO_INCREMENT,
    id_user BIGINT NOT NULL,
    id_loc BIGINT NOT NULL,
    assessment DOUBLE NOT NULL,
    commentary VARCHAR(500),
    date_create DATETIME(6) NOT NULL,
    PRIMARY KEY (id_feed),
    CONSTRAINT fk_feedbacks_user FOREIGN KEY (id_user) REFERENCES users (id_user),
    CONSTRAINT fk_feedbacks_place FOREIGN KEY (id_loc) REFERENCES places (id_loc)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
