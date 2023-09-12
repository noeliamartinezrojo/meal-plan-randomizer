DROP TABLE IF EXISTS recipes;

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

INSERT INTO recipes(
    id,
    ownerEmail,
    name,
    image,
    servingsPerBatch,
    minBatchesPerWeek,
    maxBatchesPerWeek
) VALUES(
    '11111111-2222-3333-4444-000000000001',
    'TODO@nmartinez.com',
    'someRecipeName',
    'someRecipeImage',
    4,
    1,
    2
);