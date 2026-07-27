CREATE TABLE transactions(
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL,
    category_id         BIGINT NOT NULL,
    amount              NUMERIC(19, 2) NOT NULL
                                CHECK ( amount > 0 ),
    description         VARCHAR(255),
    transaction_date    TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP WITH TIME ZONE,

    CONSTRAINT fk_transactions_user
            FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,

    CONSTRAINT fk_transactions_category
            FOREIGN KEY (category_id) REFERENCES categories (id)
);