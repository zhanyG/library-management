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
 * 图书管理员：代理读者办理借书、还书与查询（BR-013）。
 */
@Entity
@Table(name = "librarians")
public class Librarian {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "staff_no", nullable = false, unique = true, length = 32)
    private String staffNo;

    @Column(name = "name", nullable = false, length = 64)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private ReaderStatus status = ReaderStatus.ACTIVE;

    protected Librarian() {
    }

    public Librarian(String staffNo, String name) {
        this.staffNo = staffNo;
        this.name = name;
    }

    /** 停用后的管理员不能办理借还。 */
    public boolean canOperateCirculation() {
        return status == ReaderStatus.ACTIVE;
    }

    public void disable() {
        this.status = ReaderStatus.INACTIVE;
    }

    public Long getId() {
        return id;
    }

    public String getStaffNo() {
        return staffNo;
    }

    public String getName() {
        return name;
    }

    public ReaderStatus getStatus() {
        return status;
    }
}
