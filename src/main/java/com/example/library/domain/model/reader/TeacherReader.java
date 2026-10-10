package com.example.library.domain.model.reader;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * 教师读者。
 */
@Entity
@DiscriminatorValue("TEACHER")
public class TeacherReader extends Reader {

    @Column(name = "employee_no", unique = true, length = 32)
    private String employeeNo;

    @Column(name = "title", length = 32)
    private String title;

    @Column(name = "faculty", length = 64)
    private String faculty;

    protected TeacherReader() {
    }

    public TeacherReader(String name, String idCardNo, String phone, String department,
                         String employeeNo, String title, String faculty) {
        super(name, idCardNo, phone, department, ReaderType.TEACHER);
        this.employeeNo = employeeNo;
        this.title = title;
        this.faculty = faculty;
    }

    public String getEmployeeNo() {
        return employeeNo;
    }

    public String getTitle() {
        return title;
    }

    public String getFaculty() {
        return faculty;
    }
}
