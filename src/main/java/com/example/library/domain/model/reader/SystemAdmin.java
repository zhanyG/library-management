package com.example.library.domain.model.reader;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 系统管理员：维护借阅证、管理员、书目、馆藏与规则（BR-014）。
 */
@Entity
@Table(name = "system_admins")
public class SystemAdmin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 64)
    private String username;

    @Column(name = "name", nullable = false, length = 64)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private ReaderStatus status = ReaderStatus.ACTIVE;

    protected SystemAdmin() {
    }

    public SystemAdmin(String username, String name) {
        this.username = username;
        this.name = name;
    }

    public boolean isActive() {
        return status == ReaderStatus.ACTIVE;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public ReaderStatus getStatus() {
        return status;
    }
}
