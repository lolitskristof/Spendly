CREATE TABLE budgets (
id              BIGSERIAL PRIMARY KEY,
user_id         BIGINT NOT NULL,
category_id     BIGINT NOT NULL,
amount          NUMERIC(19, 2) NOT NULL
                    CHECK (amount > 0),
year            INTEGER NOT NULL,
month           INTEGER NOT NULL
                    CHECK (month BETWEEN 1 AND 12),
created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at      TIMESTAMP WITH TIME ZONE,

CONSTRAINT fk_budgets_user
    FOREIGN KEY (user_id)
    REFERENCES users (id)
    ON DELETE CASCADE,

CONSTRAINT fk_budgets_category
    FOREIGN KEY (category_id)
    REFERENCES categories (id),

CONSTRAINT uq_budgets_user_category_period
    UNIQUE (user_id, category_id, year, month)
);