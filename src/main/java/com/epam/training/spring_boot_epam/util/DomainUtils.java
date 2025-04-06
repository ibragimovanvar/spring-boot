package com.epam.training.spring_boot_epam.util;

import org.springframework.stereotype.Component;

@Component
public class DomainUtils {

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
