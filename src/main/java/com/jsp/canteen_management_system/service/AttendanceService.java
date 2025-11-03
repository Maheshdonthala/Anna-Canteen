package com.jsp.canteen_management_system.service;

import com.jsp.canteen_management_system.model.Attendance;
import com.jsp.canteen_management_system.repository.AttendanceRepository;
import com.jsp.canteen_management_system.repository.WorkerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private WorkerRepository workerRepository;

    public Attendance markAttendance(String canteenId, String workerId, String status) {
        Attendance attendance = new Attendance();
        attendance.setCanteenId(canteenId);
        attendance.setWorkerId(workerId);
        attendance.setStatus(status);
        attendance.setDate(LocalDate.now());
        workerRepository.findById(workerId).ifPresent(worker -> attendance.setWorkerName(worker.getName()));
        return attendanceRepository.save(attendance);
    }

    public List<Attendance> getAttendanceByCanteen(String canteenId) {
        return attendanceRepository.findByCanteenIdOrderByDateDesc(canteenId);
    }
}
