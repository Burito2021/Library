CREATE
EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TYPE moderation_state_type AS ENUM ('ON_REVIEW', 'APPROVED', 'DECLINED');
CREATE TYPE user_state_type AS ENUM ('ACTIVE', 'BANNED', 'SUSPENDED');
CREATE TYPE role_type_type AS ENUM ('USER', 'ADMIN');

CREATE CAST (varchar AS moderation_state_type) WITH INOUT AS IMPLICIT;
CREATE CAST (varchar AS user_state_type) WITH INOUT AS IMPLICIT;
CREATE CAST (varchar AS role_type_type) WITH INOUT AS IMPLICIT;
CREATE CAST (moderation_state_type AS varchar) WITH INOUT AS IMPLICIT;
CREATE CAST (user_state_type AS varchar) WITH INOUT AS IMPLICIT;
CREATE CAST (role_type_type AS varchar) WITH INOUT AS IMPLICIT;

CREATE TABLE USERS
(
    ID               UUID                  DEFAULT uuid_generate_v4() PRIMARY KEY,
    USERNAME         VARCHAR(200) UNIQUE                       NOT NULL,
    NAME             VARCHAR(200)                              NOT NULL,
    SURNAME          VARCHAR(200)                              NOT NULL,
    EMAIL            VARCHAR(100)                              NOT NULL,
    PHONE_NUMBER     VARCHAR(15),
    ADDRESS          VARCHAR(300),
    MODERATION_STATE moderation_state_type DEFAULT 'ON_REVIEW' NOT NULL,
    USER_STATE       user_state_type       DEFAULT 'ACTIVE'    NOT NULL,
    ROLE_TYPE        role_type_type        DEFAULT 'USER'    NOT NULL,
    UPDATED_AT       TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    CREATED_AT       TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    DELETED_AT       TIMESTAMP
);