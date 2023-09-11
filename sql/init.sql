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