package com.example.library.domain.model.reader;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * 学生读者（含专科生、本科生、研究生、博士生，细分类型见 {@link ReaderType}）。
 */
@Entity
@DiscriminatorValue("STUDENT")
public class StudentReader extends Reader {

    @Column(name = "student_no", unique = true, length = 32)
    private String studentNo;

    @Column(name = "grade", length = 16)
    private String grade;

    @Column(name = "major", length = 64)
    private String major;

    protected StudentReader() {
    }

    public StudentReader(String name, String idCardNo, String phone, String department,
                         ReaderType readerType, String studentNo, String grade, String major) {
        super(name, idCardNo, phone, department, readerType);
        this.studentNo = studentNo;
        this.grade = grade;
        this.major = major;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public String getGrade() {
        return grade;
    }

    public String getMajor() {
        return major;
    }
}
