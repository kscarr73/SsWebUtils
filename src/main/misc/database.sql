-- POSTGRESQL STYLE TABLES

CREATE TABLE users (
	loginId SERIAL,
	userName VARCHAR(50),
    password VARCHAR(100),
	fullName VARCHAR(200)
)

CREATE TABLE roles (
	roleId SERIAL,
	loginId INT,
	roleName VARCHAR(50)
)