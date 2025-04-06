package com.epam.training.spring_boot_epam.exception;

public class DomainException extends RuntimeException {
    public DomainException(String s) {
        super(s);
    }
}
