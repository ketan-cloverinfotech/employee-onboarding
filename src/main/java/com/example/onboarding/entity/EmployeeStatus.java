package com.example.onboarding.entity;

public enum EmployeeStatus {
    PRE_BOARDING("Pre-boarding"),
    IN_PROGRESS("In progress"),
    ACTIVE("Active"),
    ON_HOLD("On hold");

    private final String label;

    EmployeeStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
