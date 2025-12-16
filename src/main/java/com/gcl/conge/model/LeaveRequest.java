package com.gcl.conge.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class LeaveRequest {
    private final UUID id;
    private final String employeeId;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final LeaveType type;
    private final int year;
    private final int days;
    private ApprovalStatus status;
    private String rejectionReason;
    private Long displayId;

    public LeaveRequest(String employeeId, LocalDate startDate, LocalDate endDate, LeaveType type, int days) {
        this.id = UUID.randomUUID();
        this.employeeId = Objects.requireNonNull(employeeId, "employeeId");
        this.startDate = Objects.requireNonNull(startDate, "startDate");
        this.endDate = Objects.requireNonNull(endDate, "endDate");
        this.type = Objects.requireNonNull(type, "type");
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate cannot be before startDate");
        }
        if (days <= 0) {
            throw new IllegalArgumentException("days must be > 0");
        }
        this.days = days;
        this.year = startDate.getYear();
        this.status = ApprovalStatus.PENDING;
    }

    public LeaveRequest(UUID id, String employeeId, LocalDate startDate, LocalDate endDate,
            LeaveType type, int days, int year, ApprovalStatus status, String rejectionReason) {
        this.id = Objects.requireNonNull(id, "id");
        this.employeeId = Objects.requireNonNull(employeeId, "employeeId");
        this.startDate = Objects.requireNonNull(startDate, "startDate");
        this.endDate = Objects.requireNonNull(endDate, "endDate");
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate cannot be before startDate");
        }
        this.type = Objects.requireNonNull(type, "type");
        if (days <= 0) {
            throw new IllegalArgumentException("days must be > 0");
        }
        this.days = days;
        this.year = year;
        this.status = status == null ? ApprovalStatus.PENDING : status;
        this.rejectionReason = rejectionReason;
    }

    public UUID getId() {
        return id;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public LeaveType getType() {
        return type;
    }

    public int getYear() {
        return year;
    }

    public int getDays() {
        return days;
    }

    public ApprovalStatus getStatus() {
        return status;
    }

    public void setStatus(ApprovalStatus status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public Long getDisplayId() {
        return displayId;
    }

    public void setDisplayId(Long displayId) {
        this.displayId = displayId;
    }
}
