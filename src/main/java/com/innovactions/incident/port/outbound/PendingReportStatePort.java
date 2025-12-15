package com.innovactions.incident.port.outbound;

public interface PendingReportStatePort {

    /**
     * Marks that the user is in the process of creating a report
     * @param userId
     */
    void markPending(String userId);

    /**
     * Checks if the user is in the process of creating a report
     * @param userId
     * @return
     */
    boolean isPending(String userId);

    /**
     * Clears the pending state for the user
     * @param userId
     */
    void clearPending(String userId);

    /**
     * Marks that the user is in the process of updating a report
     * @param userId
     */
    void markUpdating(String userId);

    /**
     * Checks if the user is in the process of updating a report
     * @param userId
     * @return
     */
    boolean isUpdating(String userId);

    /**
     * Clears the updating state for the user
     * @param userId
     */
    void clearUpdating(String userId);
}
