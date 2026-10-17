package com.elouage.ligne;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VilleApi(@JsonProperty("Value") String value,
                       @JsonProperty("Delegations") List<DelegationApi> delegations) { }
