package com.kood.backend.entity.ConnectionEntities;

public enum ConnectionStatus {

    ACCEPTED("accepted"),
    REJECTED("rejected"),
    PENDING("pending"),
    BLOCKED("blocked");

    private final String value;

    ConnectionStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ConnectionStatus fromValue(String value) {
        for (ConnectionStatus status : ConnectionStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown enum value: " + value);
    }
}
