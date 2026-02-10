package de.hamburg.university.agent.tool;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public enum ToolStructuredContentType {
    SOURCE,
    DRUGST_ONE,
    DIGEST,
    FILE
}
