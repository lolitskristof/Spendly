CREATE TABLE subscriptions (
id                  BIGSERIAL PRIMARY KEY,
user_id             BIGINT NOT NULL,
category_id         BIGINT NOT NULL,
name                VARCHAR(100) NOT NULL,
amount              NUMERIC(19, 2) NOT NULL
                        CHECK (amount > 0),
billing_period      VARCHAR(20) NOT NULL
                        CHECK (billing_period IN ('MONTHLY', 'YEARLY')),
next_billing_date   DATE NOT NULL,
status              VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                        CHECK (status IN ('ACTIVE', 'CANCELLED')),
created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at          TIMESTAMP WITH TIME ZONE,

CONSTRAINT fk_subscriptions_user
            FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

CONSTRAINT fk_subscriptions_category
            FOREIGN KEY (category_id)
            REFERENCES categories (id)
);