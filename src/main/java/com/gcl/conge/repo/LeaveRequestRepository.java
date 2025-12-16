package com.gcl.conge.repo;

import com.gcl.conge.model.LeaveRequest;

import java.util.List;
import java.util.UUID;

public interface LeaveRequestRepository {
    LeaveRequest save(LeaveRequest r);

    LeaveRequest findById(UUID id);

    LeaveRequest findByDisplayId(long displayId);

    List<LeaveRequest> findByEmployee(String employeeId);

    List<LeaveRequest> findByYearAndEmployee(int year, String employeeId);

    List<LeaveRequest> findPending();

    List<LeaveRequest> findApproved();
}
