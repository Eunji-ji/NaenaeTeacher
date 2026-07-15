package com.naenae.teacher.assignment.domain;
public enum AssignmentStatus {
    IN_PROGRESS("진행중"), SCHEDULED("예정"), COMPLETED("완료");
    private final String label;
    AssignmentStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
}