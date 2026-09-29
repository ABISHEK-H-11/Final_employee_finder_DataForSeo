package com.employeeFinderByDataForSEO.Entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Entity
@Setter
@Getter
@Table(name = "account_usage")
public class AccountUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    private Account account;

    @Column(nullable = false)
    private long dailyProfileUsage = 0;

    @Column(nullable = false)
    private LocalDateTime lastResetAt;

    public AccountUsage() {
    }

    public AccountUsage(Account account, LocalDateTime lastResetAt) {
        this.account = account;
        this.lastResetAt = lastResetAt;
    }

    public AccountUsage(long dailyProfileUsage, LocalDateTime lastResetAt, Account account) {
        this.dailyProfileUsage = dailyProfileUsage;
        this.lastResetAt = lastResetAt;
        this.account = account;
    }

    public AccountUsage(Long id, Account account, long dailyProfileUsage, LocalDateTime lastResetAt) {
        this.id = id;
        this.account = account;
        this.dailyProfileUsage = dailyProfileUsage;
        this.lastResetAt = lastResetAt;
    }
}
