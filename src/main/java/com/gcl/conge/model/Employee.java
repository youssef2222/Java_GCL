package com.gcl.conge.model;

import java.util.Objects;

public class Employee {
    private final String id;
    private String name;
    private int annualEntitlementDays;

    public Employee(String id, String name, int annualEntitlementDays) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        if (annualEntitlementDays < 0) {
            throw new IllegalArgumentException("annualEntitlementDays must be >= 0");
        }
        this.annualEntitlementDays = annualEntitlementDays;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    public int getAnnualEntitlementDays() {
        return annualEntitlementDays;
    }

    public void setAnnualEntitlementDays(int annualEntitlementDays) {
        if (annualEntitlementDays < 0) {
            throw new IllegalArgumentException("annualEntitlementDays must be >= 0");
        }
        this.annualEntitlementDays = annualEntitlementDays;
    }
}

