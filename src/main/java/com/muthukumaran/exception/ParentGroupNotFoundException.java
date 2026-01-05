package com.muthukumaran.organization.exception;

public class ParentGroupNotFoundException extends RuntimeException {
    public ParentGroupNotFoundException(String parentUuid) {
        super("Parent group not found with UUID: " + parentUuid);
    }
}
