package com.redhat.developer.decision.api;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.redhat.developer.database.IEventStorage;
import com.redhat.developer.decision.responses.DecisionDetailResponse;
import com.redhat.developer.decision.responses.DecisionInputsResponse;
import com.redhat.developer.decision.responses.DecisionOutputsResponse;
import com.redhat.developer.decision.responses.ExecutionHeaderResponse;
import com.redhat.developer.decision.responses.ExecutionResponse;
import com.redhat.developer.decision.storage.model.DMNResultModel;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.jboss.resteasy.annotations.jaxrs.QueryParam;

@Path("/executions")
public class DecisionsApi {

    @Inject
    IEventStorage storageService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ExecutionResponse getExecutions(@QueryParam("from") String from,
                                           @QueryParam("to") String to,
                                           @QueryParam("limit") int limit,
                                           @QueryParam("offset") int offset) {
        List<DMNResultModel> results = storageService.getDecisions(from, to, limit, offset);
        List<ExecutionHeaderResponse> evaluationResponses = new ArrayList<>();
        results.forEach(x -> evaluationResponses.add(buildHeaderResponse(x)));
        return new ExecutionResponse(0, 0, 0, evaluationResponses);
    }

    @GET
    @Path("/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    public DecisionDetailResponse getExecutionByKey(@PathParam("key") String key) {
        DMNResultModel result = storageService.getEvent(key).data.result;
        return new DecisionDetailResponse(result.executionId, result.executionDate, buildHeaderResponse(result));
    }

    @GET
    @Path("/{key}/inputs")
    @Produces(MediaType.APPLICATION_JSON)
    public DecisionInputsResponse getExecutionInputs(@PathParam("key") String key) {
        DMNResultModel result = storageService.getEvent(key).data.result;
        return new DecisionInputsResponse(result.executionId, result.executionDate, result.context);
    }

    @GET
    @Path("/{key}/outcomes")
    @Produces(MediaType.APPLICATION_JSON)
    public DecisionOutputsResponse getExecutionOutcome(@PathParam("key") String key) {
        DMNResultModel result = storageService.getEvent(key).data.result;
        return new DecisionOutputsResponse(result.executionId, result.executionDate, result.decisions);
    }

    private ExecutionHeaderResponse buildHeaderResponse(DMNResultModel result) {
        return new ExecutionHeaderResponse(result.executionId, result.executionDate, result.decisions.stream().anyMatch(y -> y.hasErrors == true), "testUser");
    }
}