-- FROZEN (baseline). Schema of the original statement plus a surrogate key.
-- Hibernate validates against this schema (spring.jpa.hibernate.ddl-auto=validate); it never creates it.
CREATE TABLE prices (
    id          BIGINT        AUTO_INCREMENT PRIMARY KEY,
    brand_id    BIGINT        NOT NULL,
    start_date  TIMESTAMP     NOT NULL,
    end_date    TIMESTAMP     NOT NULL,
    price_list  INTEGER       NOT NULL,
    product_id  BIGINT        NOT NULL,
    priority    INTEGER       NOT NULL,
    price       DECIMAL(10,2) NOT NULL,
    curr        VARCHAR(3)    NOT NULL,
    CONSTRAINT chk_prices_window   CHECK (start_date <= end_date),
    CONSTRAINT chk_prices_priority CHECK (priority >= 0),
    CONSTRAINT chk_prices_amount   CHECK (price >= 0)
);

CREATE INDEX idx_prices_lookup ON prices (brand_id, product_id, start_date, end_date);
