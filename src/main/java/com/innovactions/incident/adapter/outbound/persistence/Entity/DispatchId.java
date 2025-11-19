package com.innovactions.incident.adapter.outbound.persistence.Entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DispatchId implements Serializable {
    private UUID developerId;
    private UUID incidentId;
}
