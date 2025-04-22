package com.epam.training.spring_boot_epam.util;

import com.epam.training.spring_boot_epam.domain.User;
import com.epam.training.spring_boot_epam.exception.DomainException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class DomainUtils {


    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            return (User) authentication.getPrincipal();
        }

        throw new DomainException("User not found");
    }

    public String getMessage(String entityName, StatusTypes messageType, OperationTypes operationTypes) {
        StringBuilder result = new StringBuilder();
        result.append(entityName).append(" ");

        if(operationTypes != null){
            result.append(operationTypes.getLabel()).append(" ");
        }

        if(messageType != null) {
            result.append(messageType.getLabel());
        }

        return result.toString();
    }
}
