package org.example.transacaggrapiapp;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.serviceclient.WorkflowServiceStubs;
import org.example.transacaggrapiapp.transactionAggregatorWorkflow.TransactionAggregatorWorkflow;

public class InitiateTransactionAggregator {

    public int initTransactionAggregator(){
        // This gRPC stubs wrapper talks to the local docker instance of the Temporal service.
        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();

        // WorkflowClient can be used to start, signal, query, cancel, and terminate Workflows.
        WorkflowClient client = WorkflowClient.newInstance(service);

        // Define our workflow unique id
        final String WORKFLOW_ID = "aggregatorID";

        /*
         * Set Workflow options such as WorkflowId and Task Queue so the worker knows where to list and which workflows to execute.
         */
        WorkflowOptions options = WorkflowOptions.newBuilder()
                .setWorkflowId(WORKFLOW_ID)
                .setTaskQueue(Shared.TRANSACTION_AGGREGATOR_TASK_QUEUE)
                .build();

        // Create the workflow client stub. It is used to start our workflow execution.
        TransactionAggregatorWorkflow workflow = client.newWorkflowStub(TransactionAggregatorWorkflow.class, options);

        /*
         * Execute our workflow and wait for it to complete. The call to our getGreeting method is
         * synchronous.
         *
         * Replace the parameter "World" in the call to getGreeting() with your name.
         */
        int aggregate = workflow.aggregate();

        String workflowId = WorkflowStub.fromTyped(workflow).getExecution().getWorkflowId();

        // Display workflow execution results
        System.out.println(workflowId + " " + aggregate);
        return aggregate;

    }

}
