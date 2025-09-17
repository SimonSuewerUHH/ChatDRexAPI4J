package de.hamburg.university.service.netdrex;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum NetdrexNodeCollection {
    @JsonProperty("disorder")
    DISORDER,
    @JsonProperty("drug")
    DRUG,
    @JsonProperty("gene")
    GENE,
    @JsonProperty("genomic_variant")
    GENOMIC_VARIANT,
    @JsonProperty("go")
    GO,
    @JsonProperty("pathway")
    PATHWAY,
    @JsonProperty("phenotype")
    PHENOTYPE,
    @JsonProperty("protein")
    PROTEIN,
    @JsonProperty("side_effect")
    SIDE_EFFECT,
    @JsonProperty("signature")
    SIGNATURE,
    @JsonProperty("tissue")
    TISSUE;
}