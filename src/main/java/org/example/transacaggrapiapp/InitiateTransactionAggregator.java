package org.example.transacaggrapiapp;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import lombok.RequiredArgsConstructor;
import org.example.transacaggrapiapp.transactionAggregatorWorkflow.TransactionAggregatorWorkflow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InitiateTransactionAggregator {

    @Value("${temporal.service-address}")
    private String temporalHost;

    public int initTransactionAggregator() {
        WorkflowServiceStubs service = WorkflowServiceStubs.newServiceStubs(
                WorkflowServiceStubsOptions.newBuilder()
                        .setTarget(temporalHost)
                        .build()
        );

        WorkflowClient client = WorkflowClient.newInstance(
                service,
                WorkflowClientOptions.newBuilder()
                        .setNamespace("default")
                        .build()
        );

        WorkflowOptions options = WorkflowOptions.newBuilder()
                .setWorkflowId("aggregatorID")
                .setTaskQueue(Shared.TRANSACTION_AGGREGATOR_TASK_QUEUE)
                .build();

        TransactionAggregatorWorkflow workflow = client.newWorkflowStub(TransactionAggregatorWorkflow.class, options);

        int aggregate = workflow.aggregate();

        String workflowId = WorkflowStub.fromTyped(workflow).getExecution().getWorkflowId();
        System.out.println(workflowId + " " + aggregate);

        return aggregate;
    }
}
