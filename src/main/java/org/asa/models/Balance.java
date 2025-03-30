package org.asa.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "balances")
@NoArgsConstructor
public class Balance {

    public Balance(long accountId, double amount, long updatedAt) {
        this.accountId = accountId;
        this.amount = amount;
        this.updatedAt = updatedAt;
    }

    @Id
    private long accountId; // Felt weird to do w/o accountId, so created a placeholder field
    private double amount;
    private long updatedAt;

    @Version
    private long version;

    public void updateBalance(double amount) {
        this.amount += amount;
    }
}
