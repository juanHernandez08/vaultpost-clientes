-- =====================================================================
-- Vaultpost - Modelo físico de la base de datos (versión MySQL 8)
-- Proyecto formativo ADSO - Ficha 3336144
-- Traducción normalizada del esquema de producción (Supabase/PostgreSQL)
-- =====================================================================

SET NAMES utf8mb4;

DROP DATABASE IF EXISTS vaultpost;
CREATE DATABASE vaultpost CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE vaultpost;

-- ---------------------------------------------------------------------
-- Agencias (tenant del SaaS)
-- ---------------------------------------------------------------------
CREATE TABLE companies (
  id          CHAR(36)     NOT NULL DEFAULT (UUID()),
  name        VARCHAR(120) NOT NULL,
  owner_id    CHAR(36)     NULL,
  plan        ENUM('free','pro','agency') NOT NULL DEFAULT 'free',
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT pk_companies PRIMARY KEY (id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Usuarios de cada agencia (la autenticación la gestiona Supabase Auth)
-- ---------------------------------------------------------------------
CREATE TABLE users (
  id                   CHAR(36)     NOT NULL,
  company_id           CHAR(36)     NOT NULL,
  email                VARCHAR(160) NOT NULL,
  full_name            VARCHAR(120) NULL,
  role                 ENUM('admin','custom') NOT NULL DEFAULT 'custom',
  can_publish          BOOLEAN      NOT NULL DEFAULT FALSE,
  can_schedule         BOOLEAN      NOT NULL DEFAULT FALSE,
  can_view_analytics   BOOLEAN      NOT NULL DEFAULT FALSE,
  can_manage_accounts  BOOLEAN      NOT NULL DEFAULT FALSE,
  created_at           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT pk_users PRIMARY KEY (id),
  CONSTRAINT uq_users_email UNIQUE (email),
  CONSTRAINT fk_users_company FOREIGN KEY (company_id)
    REFERENCES companies(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Relación circular: dueño de la agencia (se agrega después de crear users)
ALTER TABLE companies
  ADD CONSTRAINT fk_companies_owner FOREIGN KEY (owner_id)
    REFERENCES users(id) ON DELETE SET NULL;

-- ---------------------------------------------------------------------
-- Clientes de la agencia
-- ---------------------------------------------------------------------
CREATE TABLE clients (
  id          CHAR(36)     NOT NULL DEFAULT (UUID()),
  company_id  CHAR(36)     NOT NULL,
  name        VARCHAR(120) NOT NULL,
  logo_url    VARCHAR(500) NULL,
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT pk_clients PRIMARY KEY (id),
  CONSTRAINT uq_clients_company_name UNIQUE (company_id, name),
  CONSTRAINT fk_clients_company FOREIGN KEY (company_id)
    REFERENCES companies(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Clientes que puede gestionar cada usuario (N:M usuarios-clientes)
-- Reemplaza el arreglo permissions.clients del JSONB de producción
-- ---------------------------------------------------------------------
CREATE TABLE user_clients (
  user_id     CHAR(36) NOT NULL,
  client_id   CHAR(36) NOT NULL,
  granted_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT pk_user_clients PRIMARY KEY (user_id, client_id),
  CONSTRAINT fk_uc_user   FOREIGN KEY (user_id)   REFERENCES users(id)   ON DELETE CASCADE,
  CONSTRAINT fk_uc_client FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Cuentas de redes sociales de cada cliente
-- ---------------------------------------------------------------------
CREATE TABLE social_accounts (
  id                CHAR(36)     NOT NULL DEFAULT (UUID()),
  client_id         CHAR(36)     NOT NULL,
  platform          ENUM('instagram','facebook','tiktok') NOT NULL,
  username          VARCHAR(80)  NOT NULL,
  credentials_enc   TEXT         NULL COMMENT 'Cifrado AES-256-GCM',
  access_token_enc  TEXT         NULL COMMENT 'Cifrado AES-256-GCM',
  last_sync         DATETIME     NULL,
  is_active         BOOLEAN      NOT NULL DEFAULT TRUE,
  added_by_id       CHAR(36)     NULL,
  created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT pk_social_accounts PRIMARY KEY (id),
  CONSTRAINT uq_social_platform_user UNIQUE (platform, username),
  CONSTRAINT fk_sa_client FOREIGN KEY (client_id)  REFERENCES clients(id) ON DELETE CASCADE,
  CONSTRAINT fk_sa_added_by FOREIGN KEY (added_by_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Publicaciones programadas
-- ---------------------------------------------------------------------
CREATE TABLE scheduled_posts (
  id                 CHAR(36)     NOT NULL DEFAULT (UUID()),
  social_account_id  CHAR(36)     NOT NULL,
  content_type       ENUM('post','reel','story','video') NOT NULL,
  caption            TEXT         NULL,
  media_url          VARCHAR(500) NULL,
  scheduled_at       DATETIME     NOT NULL,
  status             ENUM('draft','scheduled','publishing','published','failed') NOT NULL DEFAULT 'scheduled',
  error_msg          TEXT         NULL,
  created_by_id      CHAR(36)     NULL,
  created_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT pk_scheduled_posts PRIMARY KEY (id),
  CONSTRAINT chk_caption_len CHECK (caption IS NULL OR CHAR_LENGTH(caption) <= 2200),
  CONSTRAINT fk_sp_account FOREIGN KEY (social_account_id) REFERENCES social_accounts(id) ON DELETE CASCADE,
  CONSTRAINT fk_sp_created_by FOREIGN KEY (created_by_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- Índice para el cron de publicación: busca posts 'scheduled' con fecha vencida
CREATE INDEX idx_sp_status_date ON scheduled_posts (status, scheduled_at);

-- ---------------------------------------------------------------------
-- Métricas de las publicaciones (una lectura por sincronización)
-- ---------------------------------------------------------------------
CREATE TABLE post_metrics (
  id                 CHAR(36)      NOT NULL DEFAULT (UUID()),
  scheduled_post_id  CHAR(36)      NOT NULL,
  likes              INT UNSIGNED  NOT NULL DEFAULT 0,
  comments           INT UNSIGNED  NOT NULL DEFAULT 0,
  shares             INT UNSIGNED  NOT NULL DEFAULT 0,
  reach              INT UNSIGNED  NOT NULL DEFAULT 0,
  engagement_rate    DECIMAL(5,2)  NOT NULL DEFAULT 0,
  fetched_at         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT pk_post_metrics PRIMARY KEY (id),
  CONSTRAINT chk_engagement CHECK (engagement_rate BETWEEN 0 AND 100),
  CONSTRAINT fk_pm_post FOREIGN KEY (scheduled_post_id) REFERENCES scheduled_posts(id) ON DELETE CASCADE
) ENGINE=InnoDB;
