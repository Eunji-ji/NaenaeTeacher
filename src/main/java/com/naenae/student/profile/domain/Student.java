package com.naenae.student.profile.domain;

import com.naenae.common.domain.BaseTimeEntity;
import com.naenae.teacher.profile.domain.Teacher;
import com.naenae.common.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "students")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Student extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "school_name", length = 150)
    private String schoolName;

    @Column(length = 50)
    private String grade;

    @Column(length = 30)
    private String phone;

    @Column(name = "parent_phone", length = 30)
    private String parentPhone;

    @Column(name = "memo_summary", columnDefinition = "TEXT")
    private String memoSummary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StudentStatus status = StudentStatus.ACTIVE;

    public static Student create(Teacher teacher, String name, String schoolName, String phone) {
        Student student = new Student();
        student.teacher = teacher;
        student.name = name;
        student.schoolName = schoolName;
        student.phone = phone;
        student.status = StudentStatus.ACTIVE;
        return student;
    }

    public Long getId() {
        return id;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public String getName() {
        return name;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public String getPhone() {
        return phone;
    }

    public String getMemoSummary() {
        return memoSummary;
    }

    public void updateMemoSummary(String memoSummary) {
        this.memoSummary = memoSummary;
    }

    public void connectUser(User user) {
        if (this.user != null) {
            throw new IllegalStateException("이미 계정이 연결된 학생입니다.");
        }
        this.user = user;
    }
}
