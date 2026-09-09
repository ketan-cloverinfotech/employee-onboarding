package com.example.onboarding.entity;

public enum TaskCategory {
    HR("HR"),
    DOCUMENTATION("Documentation"),
    IT_SETUP("IT setup"),
    MANAGER("Manager"),
    TRAINING("Training");

    private final String label;

    TaskCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
