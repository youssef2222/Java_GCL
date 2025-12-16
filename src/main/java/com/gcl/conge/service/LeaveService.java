package com.gcl.conge.service;

import com.gcl.conge.model.*;
import com.gcl.conge.repo.EmployeeRepository;
import com.gcl.conge.repo.LeaveRequestRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class LeaveService {
    private final EmployeeRepository employeeRepo;
    private final LeaveRequestRepository requestRepo;

    public LeaveService(EmployeeRepository employeeRepo, LeaveRequestRepository requestRepo) {
        this.employeeRepo = employeeRepo;
        this.requestRepo = requestRepo;
    }

    public Employee addEmployee(String id, String name, int annualEntitlementDays) {
        Employee e = new Employee(id, name, annualEntitlementDays);
        return employeeRepo.save(e);
    }

    public Employee getEmployee(String id) {
        return employeeRepo.findById(id);
    }

    public LeaveRequest requestLeave(String employeeId, LocalDate start, LocalDate end, LeaveType type) {
        validateEmployee(employeeId);
        int days = businessDaysBetween(start, end);
        LeaveRequest req = new LeaveRequest(employeeId, start, end, type, days);
        return requestRepo.save(req);
    }

    public LeaveRequest approveRequest(UUID requestId) {
        LeaveRequest req = requestRepo.findById(requestId);
        if (req == null) {
            throw new IllegalArgumentException("Request not found: " + requestId);
        }
        if (req.getStatus() != ApprovalStatus.PENDING) {
            return req;
        }
        req.setStatus(ApprovalStatus.APPROVED);
        if (req.getType() == LeaveType.ANNUAL) {
            // update used days implicitly by computing remaining from history
        }
        requestRepo.save(req);
        return req;
    }

    public LeaveRequest rejectRequest(UUID requestId, String reason) {
        LeaveRequest req = requestRepo.findById(requestId);
        if (req == null) {
            throw new IllegalArgumentException("Request not found: " + requestId);
        }
        req.setStatus(ApprovalStatus.REJECTED);
        req.setRejectionReason(reason);
        requestRepo.save(req);
        return req;
    }

    public LeaveRequest approveByDisplayId(long displayId) {
        LeaveRequest req = requestRepo.findByDisplayId(displayId);
        if (req == null) {
            throw new IllegalArgumentException("Request not found: " + displayId);
        }
        return approveRequest(req.getId());
    }

    public LeaveRequest rejectByDisplayId(long displayId, String reason) {
        LeaveRequest req = requestRepo.findByDisplayId(displayId);
        if (req == null) {
            throw new IllegalArgumentException("Request not found: " + displayId);
        }
        return rejectRequest(req.getId(), reason);
    }

    public int getAnnualRemainingDays(String employeeId, int year) {
        Employee e = validateEmployee(employeeId);
        int used = requestRepo.findByYearAndEmployee(year, employeeId).stream()
                .filter(r -> r.getType() == LeaveType.ANNUAL && r.getStatus() == ApprovalStatus.APPROVED)
                .mapToInt(LeaveRequest::getDays)
                .sum();
        int entitlement = e.getAnnualEntitlementDays();
        int remaining = entitlement - used;
        return Math.max(0, remaining);
    }

    public List<LeaveRequest> listPendingRequests() {
        return requestRepo.findPending();
    }

    public List<LeaveRequest> listApprovedRequests() {
        return requestRepo.findApproved();
    }

    private Employee validateEmployee(String employeeId) {
        Employee e = employeeRepo.findById(employeeId);
        if (e == null)
            throw new IllegalArgumentException("Employee not found: " + employeeId);
        return e;
    }

    private int businessDaysBetween(LocalDate start, LocalDate end) {
        int days = 0;
        LocalDate d = start;
        while (!d.isAfter(end)) {
            DayOfWeek dow = d.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                days++;
            }
            d = d.plusDays(1);
        }
        return days;
    }
}
