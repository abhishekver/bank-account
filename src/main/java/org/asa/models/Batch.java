package org.asa.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "batches")
@NoArgsConstructor
public class Batch {

    public Batch(double totalValue, int transactionCount) {
        this.totalValue = totalValue;
        this.transactionCount = transactionCount;
        this.createdAt = System.currentTimeMillis();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String batchId;
    private double totalValue;
    private int transactionCount;
    private long createdAt;
    private long updatedAt;
}
