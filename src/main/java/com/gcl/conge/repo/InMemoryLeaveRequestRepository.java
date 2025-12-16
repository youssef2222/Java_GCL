package com.gcl.conge.repo;

import com.gcl.conge.model.ApprovalStatus;
import com.gcl.conge.model.LeaveRequest;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryLeaveRequestRepository implements LeaveRequestRepository {
    private final Map<UUID, LeaveRequest> requests = new ConcurrentHashMap<>();
    private final java.util.concurrent.atomic.AtomicLong seq = new java.util.concurrent.atomic.AtomicLong(1);

    @Override
    public LeaveRequest save(LeaveRequest r) {
        if (r.getDisplayId() == null) {
            r.setDisplayId(seq.getAndIncrement());
        }
        requests.put(r.getId(), r);
        return r;
    }

    @Override
    public LeaveRequest findById(UUID id) {
        return requests.get(id);
    }

    @Override
    public LeaveRequest findByDisplayId(long displayId) {
        return requests.values().stream().filter(x -> x.getDisplayId() != null && x.getDisplayId() == displayId)
                .findFirst().orElse(null);
    }

    @Override
    public List<LeaveRequest> findByEmployee(String employeeId) {
        return requests.values().stream()
                .filter(r -> r.getEmployeeId().equals(employeeId))
                .sorted(Comparator.comparing(LeaveRequest::getStartDate))
                .collect(Collectors.toList());
    }

    @Override
    public List<LeaveRequest> findByYearAndEmployee(int year, String employeeId) {
        return requests.values().stream()
                .filter(r -> r.getYear() == year && r.getEmployeeId().equals(employeeId))
                .sorted(Comparator.comparing(LeaveRequest::getStartDate))
                .collect(Collectors.toList());
    }

    @Override
    public List<LeaveRequest> findPending() {
        return requests.values().stream()
                .filter(r -> r.getStatus() == ApprovalStatus.PENDING)
                .sorted(Comparator.comparing(LeaveRequest::getStartDate))
                .collect(Collectors.toList());
    }

    @Override
    public List<LeaveRequest> findApproved() {
        return requests.values().stream()
                .filter(r -> r.getStatus() == ApprovalStatus.APPROVED)
                .sorted(Comparator.comparing(LeaveRequest::getStartDate))
                .collect(Collectors.toList());
    }
}
