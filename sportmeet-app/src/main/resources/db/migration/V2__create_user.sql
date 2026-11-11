-- Initial account schema for fresh development databases.
-- IMPORTANT: Do not modify an already applied Flyway migration in shared databases.
-- An existing V2 with UUID identifiers requires a separate data-preserving migration.
CREATE TABLE T_USER
(
    ID                      BIGINT GENERATED ALWAYS AS IDENTITY NOT NULL,
    LOGIN                   VARCHAR(80)                         NOT NULL,
    DISPLAY_NAME            VARCHAR(255)                        NOT NULL,
    EMAIL                   VARCHAR(254)                        NOT NULL,
    PASSWORD_HASH           VARCHAR(255)                        NOT NULL,
    STATE                   VARCHAR(32)                         NOT NULL,
    VALID                   BOOLEAN DEFAULT TRUE                NOT NULL,
    CREATED_AT              TIMESTAMP(6) WITH TIME ZONE         NOT NULL,
    CREATED_BY_OBJECT_ID    BIGINT,
    CREATED_BY_OBJECT_TYPE  VARCHAR(32)                         NOT NULL,
    MODIFIED_AT             TIMESTAMP(6) WITH TIME ZONE,
    MODIFIED_BY_OBJECT_ID   BIGINT,
    MODIFIED_BY_OBJECT_TYPE VARCHAR(32),
    VERSION                 BIGINT                              NOT NULL,
    CONSTRAINT PK_T_USER PRIMARY KEY (ID),
    CONSTRAINT UQ_T_USER_LOGIN UNIQUE (LOGIN),
    CONSTRAINT UQ_T_USER_EMAIL UNIQUE (EMAIL),
    CONSTRAINT CK_T_USER_STATE CHECK (STATE IN ('ACTIVE', 'DISABLED', 'BLOCKED')),
    CONSTRAINT CK_T_USER_CREATED_BY_TYPE CHECK (CREATED_BY_OBJECT_TYPE IN ('USER', 'SYSTEM')),
    CONSTRAINT CK_T_USER_MODIFIED_BY_TYPE CHECK (MODIFIED_BY_OBJECT_TYPE IS NULL OR MODIFIED_BY_OBJECT_TYPE IN ('USER', 'SYSTEM'))
);
