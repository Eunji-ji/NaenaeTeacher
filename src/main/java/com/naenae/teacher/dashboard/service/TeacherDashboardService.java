package com.naenae.teacher.dashboard.service;

import com.naenae.teacher.dashboard.model.TeacherDashboard;
import org.springframework.stereotype.Service;

@Service
public class TeacherDashboardService {

    public TeacherDashboard getDashboard() {
        return new TeacherDashboard(0, 0, 0, 0);
    }
}
