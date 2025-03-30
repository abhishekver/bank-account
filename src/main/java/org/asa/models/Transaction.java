package org.asa.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@Table(name = "transactions")
public class Transaction {

    public Transaction(final String transactionId, final double amount) {
        this.transactionId = transactionId;
        this.timestamp = System.currentTimeMillis();
        this.amount = amount;
    }

    @Id
    private String transactionId;
    double amount;
    private long timestamp;

    @ManyToOne
    @JoinColumn(name = "batch_id")
    private Batch batch;
}
