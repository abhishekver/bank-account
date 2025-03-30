## Tables DDL

### Transactions
```
create table transactions(
transaction_id varchar(50) primary key,
timestamp timestamp not null,
amount decimal(15, 5) not null
)
```

### Batches
```
create table batches (
batch_id varchar(255) primary key,
total_value decimal(15,5) not null,
transaction_count int not null,
created_at timestamp not null,
updated_at timestamp not null
)
```

### Balances
```
create table balances (
account_id long primary key,
amount decimal(15,5) not null
)
```