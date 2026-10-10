package com.example.library.domain.model.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * 论文副本：默认不可外借（loanable = false）。
 */
@Entity
@DiscriminatorValue("THESIS")
public class Thesis extends LibraryItem {

    @Column(name = "degree", length = 32)
    private String degree;

    @Column(name = "supervisor", length = 64)
    private String supervisor;

    @Column(name = "department", length = 64)
    private String department;

    protected Thesis() {
    }

    public Thesis(String barcode, String location, String degree, String supervisor, String department) {
        super(barcode, ItemType.THESIS, location);
        this.degree = degree;
        this.supervisor = supervisor;
        this.department = department;
    }

    public String getDegree() {
        return degree;
    }

    public String getSupervisor() {
        return supervisor;
    }

    public String getDepartment() {
        return department;
    }
}
