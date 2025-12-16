package com.gcl.conge.model;

public class LeaveBalance {
    private final String employeeId;
    private final int year;
    private int annualEntitlement;
    private int annualUsed;

    public LeaveBalance(String employeeId, int year, int annualEntitlement) {
        this.employeeId = employeeId;
        this.year = year;
        this.annualEntitlement = annualEntitlement;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public int getYear() {
        return year;
    }

    public int getAnnualEntitlement() {
        return annualEntitlement;
    }

    public void setAnnualEntitlement(int annualEntitlement) {
        this.annualEntitlement = annualEntitlement;
    }

    public int getAnnualUsed() {
        return annualUsed;
    }

    public void addAnnualUsed(int days) {
        this.annualUsed += days;
    }

    public int getAnnualRemaining() {
        return Math.max(0, annualEntitlement - annualUsed);
    }
}

