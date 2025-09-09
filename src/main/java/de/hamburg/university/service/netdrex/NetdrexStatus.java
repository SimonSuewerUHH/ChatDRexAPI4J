package de.hamburg.university.service.netdrex;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum NetdrexStatus {
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
