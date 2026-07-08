package com.naenae.teacher.student.model;

import java.util.List;

public record StudentListItem(
        Long id,
        String name,
        String schoolName,
        String phone,
        List<String> courseNames
) {
}
