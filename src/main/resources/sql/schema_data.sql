CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE USERS
(
    ID UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    USERNAME VARCHAR(200) UNIQUE NOT NULL,
    NAME VARCHAR(200) NOT NULL,
    SURNAME VARCHAR(200) NOT NULL,
    EMAIL VARCHAR(100) NOT NULL,
    PHONE_NUMBER VARCHAR(15),
    ADDRESS VARCHAR(300),
    UPDATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    DELETED_AT TIMESTAMP
);