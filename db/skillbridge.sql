-- SkillBridge Database Schema
-- Run: mysql -u root -p < db/skillbridge.sql
--
-- FIX LOG:
--  - Added score column to trainee_skills (needed by SkillAssessor to persist results)
--  - Added completion_percent to trainees table (kept in sync with updateProgress())
--  - Added seed data block so LoginFlowTestRunner works out of the box

CREATE DATABASE IF NOT EXISTS skillbridge;
USE skillbridge;

-- ── Trainers ──────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS trainers (
    user_id       VARCHAR(20)  PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- ── Trainees ──────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS trainees (
    user_id            VARCHAR(20)  PRIMARY KEY,
    name               VARCHAR(100) NOT NULL,
    email              VARCHAR(150) NOT NULL UNIQUE,
    password_hash      VARCHAR(255) NOT NULL,
    enrolled_course    VARCHAR(200),
    trainer_id         VARCHAR(20)  REFERENCES trainers(user_id),
    completion_percent INT          DEFAULT 0,   -- FIX: added; mirrors Trainee.completionPercent
    created_at         TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- ── Skills ────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS skills (
    skill_id    VARCHAR(20)  PRIMARY KEY,
    skill_name  VARCHAR(150) NOT NULL,
    category    ENUM('ELECTRICAL','PLUMBING','CARPENTRY',
                     'TAILORING','DIGITAL_LITERACY','OTHER') NOT NULL
);

-- ── Trainee-Skill mapping ─────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS trainee_skills (
    trainee_id  VARCHAR(20)  REFERENCES trainees(user_id),
    skill_id    VARCHAR(20)  REFERENCES skills(skill_id),
    score       INT          DEFAULT 0,      -- FIX: added; stores assessment score (0-100)
    completed   BOOLEAN      DEFAULT FALSE,
    updated_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (trainee_id, skill_id)
);

-- ════════════════════════════════════════════════════════════════════════════════
-- SEED DATA — for LoginFlowTestRunner and manual testing
-- ════════════════════════════════════════════════════════════════════════════════
-- NOTE: password_hash values here are plain-text passwords stored as-is for M1.
--       TODO M2: replace with BCrypt hashes before any real deployment.
--       BCrypt hash of "abc123"  →  $2a$10$...  (generate with BCrypt.hashpw)

-- Seed trainer
INSERT IGNORE INTO trainers (user_id, name, email, password_hash)
VALUES ('TR-001', 'Arun Sharma', 'arun@skillbridge.com', 'train123');

-- Seed trainee (riya@skillbridge.com used in LoginFlowTestRunner)
INSERT IGNORE INTO trainees (user_id, name, email, password_hash, trainer_id)
VALUES ('TN-001', 'Riya Patel', 'riya@skillbridge.com', 'abc123', 'TR-001');

-- Seed skills
INSERT IGNORE INTO skills (skill_id, skill_name, category) VALUES
('SK-001', 'Basic Wiring',           'ELECTRICAL'),
('SK-002', 'Circuit Breaker Repair', 'ELECTRICAL'),
('SK-003', 'Pipe Fitting',           'PLUMBING'),
('SK-004', 'Spreadsheet Basics',     'DIGITAL_LITERACY');

-- Enrol Riya in two skills
INSERT IGNORE INTO trainee_skills (trainee_id, skill_id) VALUES
('TN-001', 'SK-001'),
('TN-001', 'SK-004');
