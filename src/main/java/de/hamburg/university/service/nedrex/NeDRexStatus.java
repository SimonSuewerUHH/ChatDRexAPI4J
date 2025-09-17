package de.hamburg.university.service.nedrex;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum NeDRexStatus {
    @JsonProperty(value = "submitted")
    SUBMITTED,
    @JsonProperty(value = "building")
    BUILDING,
    @JsonProperty(value = "failed")
    FAILED,
    @JsonProperty(value = "completed")
    COMPLETED,
    @JsonProperty(value = "running")
    RUNNING
}
