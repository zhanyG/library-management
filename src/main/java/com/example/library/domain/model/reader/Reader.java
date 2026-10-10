package com.example.library.domain.model.reader;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 读者（抽象实体）。
 *
 * <p>继承映射：单表策略（与 specs/13-database-design.md 一致），
 * 鉴别列 {@code reader_kind} 区分 StudentReader 与 TeacherReader，
 * {@code reader_type} 列保存业务上的细分读者类型（5 种）。</p>
 */
@Entity
@Table(name = "readers")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "reader_kind", discriminatorType = DiscriminatorType.STRING)
public abstract class Reader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 64)
    private String name;

    @Column(name = "id_card_no", nullable = false, unique = true, length = 32)
    private String idCardNo;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "department", nullable = false, length = 64)
    private String department;

    @Enumerated(EnumType.STRING)
    @Column(name = "reader_type", nullable = false, length = 32)
    private ReaderType readerType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private ReaderStatus status = ReaderStatus.ACTIVE;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected Reader() {
    }

    protected Reader(String name, String idCardNo, String phone, String department, ReaderType readerType) {
        this.name = name;
        this.idCardNo = idCardNo;
        this.phone = phone;
        this.department = department;
        this.readerType = readerType;
    }

    /** 账号是否可用。 */
    public boolean isActive() {
        return status == ReaderStatus.ACTIVE;
    }

    /** 停用账号。 */
    public void deactivate() {
        this.status = ReaderStatus.INACTIVE;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIdCardNo() {
        return idCardNo;
    }

    public String getPhone() {
        return phone;
    }

    public String getDepartment() {
        return department;
    }

    public ReaderType getReaderType() {
        return readerType;
    }

    public ReaderStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
