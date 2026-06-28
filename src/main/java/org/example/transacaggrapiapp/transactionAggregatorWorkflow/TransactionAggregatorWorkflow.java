package org.example.transacaggrapiapp.transactionAggregatorWorkflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface TransactionAggregatorWorkflow {

    @WorkflowMethod
    public int aggregate();
}
