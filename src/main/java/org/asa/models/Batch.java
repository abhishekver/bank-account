package org.asa.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "batches")
@NoArgsConstructor
public class Batch {

    public Batch(String batchId, double totalValue, int transactionCount, AuditLog auditLog) {
        this.batchId = batchId;
        this.totalValue = totalValue;
        this.transactionCount = transactionCount;
        this.createdAt = System.currentTimeMillis();
        this.auditLog = auditLog;
    }

    @Id
    private String batchId;
    private double totalValue;
    private int transactionCount;
    private long createdAt;

    @ManyToOne
    @JoinColumn(name = "audit_log_id")
    private AuditLog auditLog;
}
