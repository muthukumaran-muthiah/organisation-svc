package com.muthukumaran.organization.exception;

public class GroupNotFoundException extends RuntimeException {
    public GroupNotFoundException(String uuid) {
        super("Group not found with UUID: " + uuid);
    }
}
