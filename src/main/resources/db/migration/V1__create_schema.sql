-- Author: Athenkosi Mvimbi
-- All amounts are in ZAR, so there is deliberately no currency column.
-- Signed amounts: credits are positive, debits are negative.

CREATE TABLE customer (
    id           BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    external_ref VARCHAR(40)  NOT NULL,
    full_name    VARCHAR(120) NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_customer_ref UNIQUE (external_ref)
);

CREATE TABLE transaction_record (
    id             BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    customer_id    BIGINT        NOT NULL,
    source         VARCHAR(30)   NOT NULL,
    external_id    VARCHAR(64)   NOT NULL,
    booked_on      DATE          NOT NULL,
    description    VARCHAR(255)  NOT NULL,
    merchant       VARCHAR(120)  NOT NULL,
    amount         DECIMAL(15,2) NOT NULL,
    category       VARCHAR(30)   NOT NULL,
    categorised_by VARCHAR(10)   NOT NULL,
    created_at     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_txn_customer FOREIGN KEY (customer_id) REFERENCES customer (id),
    -- makes re-syncing idempotent: the same source row can never be stored twice
    CONSTRAINT uq_txn_source_ext UNIQUE (customer_id, source, external_id),
    CONSTRAINT chk_txn_amount_nonzero CHECK (amount <> 0)
);

CREATE INDEX idx_txn_customer_date     ON transaction_record (customer_id, booked_on);
CREATE INDEX idx_txn_customer_category ON transaction_record (customer_id, category);
CREATE INDEX idx_txn_customer_merchant ON transaction_record (customer_id, merchant);
