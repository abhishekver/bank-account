## Tables DDL

### Audit Logs
```
create table audit_logs(
    audit_log_id varchar(50) primary key,
    timestamp timestamp not null,
    total_amount decimal(15, 5) not null,
    batch_count int not null
)
```

### Batches
```
CREATE TABLE batches (
     batch_id VARCHAR(255) PRIMARY KEY,
     total_value DECIMAL(15,5) NOT NULL,
     transaction_count INT NOT NULL,
     created_at TIMESTAMP NOT NULL,
     audit_log_id VARCHAR(255),
     CONSTRAINT fk_audit_log FOREIGN KEY (audit_log_id) REFERENCES audit_logs(audit_log_id)
);
```

### Transactions
```
CREATE TABLE transactions (
    transaction_id VARCHAR(255) PRIMARY KEY,
    timestamp timestamp not null,
    amount decimal(15, 5) not null,
    batch_id VARCHAR(255) null,
    CONSTRAINT fk_batch FOREIGN KEY (batch_id) REFERENCES batches(batch_id)
);
```

### Balances
```
create table balances (
    account_id long primary key,
    amount decimal(15,5) not null,
    updated_at TIMESTAMP NOT NULL
)
```