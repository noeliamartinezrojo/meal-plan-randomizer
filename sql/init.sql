CREATE DATABASE mealplanrandomizer;
\c mealplanrandomizer;

CREATE TABLE recipes(
    id uuid DEFAULT gen_random_uuid(),
    ownerEmail text NOT NULL,
    name text NOT NULL,
    image text,
    servingsPerBatch integer,
    minBatchesPerWeek integer,
    maxBatchesPerWeek integer
);

ALTER TABLE recipes
ADD CONSTRAINT pk_recipes PRIMARY KEY(id);

DROP TABLE IF EXISTS users;

CREATE TABLE users(
    email text NOT NULL,
    hashedPassword text NOT NULL,
    firstName text,
    lastName text,
    role text NOT NULL
);

ALTER TABLE users
ADD CONSTRAINT pk_users PRIMARY KEY(email);

INSERT INTO users(
    email,
    hashedPassword,
    firstName,
    lastName,
    role
) VALUES(
    'noeliamtzrojo@gmail.com',
    'password',
    'Noelia',
    'Martinez Rojo',
    'ADMIN'
);

INSERT INTO users(
    email,
    hashedPassword,
    firstName,
    lastName,
    role
) VALUES(
    'janesmith@fakeuser.com',
    'password',
    'Jane',
    'Smith',
    'CLIENT'
);