package com.example.onboarding.entity;

public enum EmploymentType {
    FULL_TIME("Full time"),
    PART_TIME("Part time"),
    CONTRACT("Contract"),
    INTERN("Intern");

    private final String label;

    EmploymentType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
