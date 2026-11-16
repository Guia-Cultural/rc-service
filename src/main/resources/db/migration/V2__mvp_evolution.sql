-- V2__mvp_evolution.sql
-- Migração aditiva e não destrutiva para conclusão do MVP

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS native_requested BIT(1) DEFAULT b'0';

ALTER TABLE places
    ADD COLUMN IF NOT EXISTS estimated_cost DECIMAL(10, 2) NULL,
    ADD COLUMN IF NOT EXISTS cover_image VARCHAR(500) NULL,
    ADD COLUMN IF NOT EXISTS official_website VARCHAR(300) NULL,
    ADD COLUMN IF NOT EXISTS ticket_link VARCHAR(300) NULL,
    ADD COLUMN IF NOT EXISTS contact VARCHAR(150) NULL,
    ADD COLUMN IF NOT EXISTS rejection_reason VARCHAR(500) NULL;

CREATE TABLE IF NOT EXISTS place_images (
    id_image BIGINT NOT NULL AUTO_INCREMENT,
    id_loc BIGINT NOT NULL,
    url VARCHAR(500) NOT NULL,
    original_name VARCHAR(255),
    content_type VARCHAR(100),
    size_bytes BIGINT,
    is_cover BIT(1) DEFAULT b'0',
    date_create DATETIME(6) NOT NULL,
    PRIMARY KEY (id_image),
    CONSTRAINT fk_place_images_place FOREIGN KEY (id_loc) REFERENCES places (id_loc)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Regra de negócio RN06: uma avaliação por usuário e local (executar caso ainda não exista)
ALTER TABLE feedbacks
    ADD CONSTRAINT uk_feedbacks_user_place UNIQUE (id_user, id_loc);
