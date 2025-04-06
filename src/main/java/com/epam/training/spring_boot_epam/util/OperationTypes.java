package com.epam.training.spring_boot_epam.util;

public enum OperationTypes {
    CREATION("created"),
    READ("found"),
    UPDATE("updated"),
    DELETION("deleted"),
    END("!"),;

    private final String label;

    OperationTypes(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
