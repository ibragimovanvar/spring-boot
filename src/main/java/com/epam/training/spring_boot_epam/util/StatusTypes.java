package com.epam.training.spring_boot_epam.util;

public enum StatusTypes {
    MESSAGE_TYPE_SUCCESS("successfully !"),
    MESSAGE_TYPE_NOT_FOUND("not found !"),
    MESSAGE_TYPE_FAILURE("failed !");

    private final String label;

    StatusTypes(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
