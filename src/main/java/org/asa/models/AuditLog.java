package org.asa.models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;


@Data
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    public AuditLog() {
        this.auditLogId = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
    }

    @Id
    private String auditLogId;
    private long timestamp;
    private double totalAmount;
    private int batchCount;
}