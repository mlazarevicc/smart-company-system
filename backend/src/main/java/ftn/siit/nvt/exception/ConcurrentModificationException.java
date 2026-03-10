package ftn.siit.nvt.exception;

public class ConcurrentModificationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private String entityName;
    private Long entityId;
    private Long currentVersion;
    private Long expectedVersion;

    public ConcurrentModificationException(
            String entityName,
            Long entityId,
            Long expectedVersion,
            Long currentVersion
    ) {
        super(String.format(
                "Concurrent modification detected for %s with ID %d. " +
                        "Expected version: %d, Current version: %d. " +
                        "The resource was modified by another user.",
                entityName, entityId, expectedVersion, currentVersion
        ));
        this.entityName = entityName;
        this.entityId = entityId;
        this.expectedVersion = expectedVersion;
        this.currentVersion = currentVersion;
    }

    public ConcurrentModificationException(String resourceName, Long resourceId, String customMessage) {
        super(String.format("Concurrent modification detected for %s [id=%s]. %s",
                resourceName, resourceId, customMessage));
        this.entityName = resourceName;
        this.entityId = resourceId;
    }

    public ConcurrentModificationException(String message) {
        super(message);
    }

    public ConcurrentModificationException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getEntityName() {
        return entityName;
    }

    public Long getEntityId() {
        return entityId;
    }

    public Long getCurrentVersion() {
        return currentVersion;
    }

    public Long getExpectedVersion() {
        return expectedVersion;
    }
}