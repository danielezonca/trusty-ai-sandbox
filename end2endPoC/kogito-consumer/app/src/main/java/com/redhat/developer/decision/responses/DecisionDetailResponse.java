package com.redhat.developer.decision.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DecisionDetailResponse {
    @JsonProperty("data")
    public ExecutionHeaderResponse data;

    @JsonProperty("evaluationDate")
    public String evaluationDate;

    @JsonProperty("id")
    public String id;

    public DecisionDetailResponse(String id, String evaluationDate, ExecutionHeaderResponse data){
        this.id = id;
        this.data = data;
        this.evaluationDate = evaluationDate;
    }
}