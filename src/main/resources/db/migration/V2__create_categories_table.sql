CREATE TABLE categories (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    type            VARCHAR(20) NOT NULL
                        CHECK ( type IN ('INCOME', 'EXPENSE') ),
    user_id         BIGINT,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE,

    CONSTRAINT fk_categories_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX uq_categories_user_name_type
    ON categories (user_id, name, type)
    WHERE user_id IS NOT NULL;

CREATE UNIQUE INDEX uq_categories_system_name_type
    ON categories (name, type)
    WHERE user_id IS NULL;