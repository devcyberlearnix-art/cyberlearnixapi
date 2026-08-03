CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL,
    course_id UUID NOT NULL,
    rating INTEGER NOT NULL,
    comment TEXT,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_user_course UNIQUE (user_id, course_id)
);

CREATE INDEX idx_reviews_course_id ON reviews (course_id);
CREATE INDEX idx_reviews_user_id ON reviews (user_id);
CREATE INDEX idx_reviews_status ON reviews (status);
