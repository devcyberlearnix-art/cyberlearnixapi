ALTER TABLE reviews
    ADD COLUMN review_uuid UUID;

UPDATE reviews
SET review_uuid = md5(random()::text || clock_timestamp()::text)::uuid
WHERE review_uuid IS NULL;

ALTER TABLE reviews
    ALTER COLUMN review_uuid SET NOT NULL;

CREATE UNIQUE INDEX idx_reviews_uuid ON reviews (review_uuid);
