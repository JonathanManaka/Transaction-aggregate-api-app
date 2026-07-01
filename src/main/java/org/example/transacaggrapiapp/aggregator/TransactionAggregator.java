package org.example.transacaggrapiapp.aggregator;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.transacaggrapiapp.TransactionAggregatorActivity.TransactionAggregatorActivity;
import org.example.transacaggrapiapp.model.Transaction;
import org.example.transacaggrapiapp.transactionAggregatorWorkflow.TransactionAggregatorWorkflow;
import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Slf4j
public class TransactionAggregator implements TransactionAggregatorWorkflow {

    ActivityOptions options = ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(60))
            .build();

    private final TransactionAggregatorActivity activity = Workflow.newActivityStub(TransactionAggregatorActivity.class, options);

    @Override
    public int aggregate() {

        int saveCount = 0;
        List<Transaction> rawTransactions = new ArrayList<>();

        rawTransactions.addAll(activity.fetchBankBTransactions());
        rawTransactions.addAll(activity.fetchBankATransactions());

        for (Transaction tx : rawTransactions) {
        if (activity.isPresent(tx)) {
            log.debug("Skipping duplicate: {} from {}", tx.getExternalId(), tx.getSource());
            continue;
        }
            tx = activity.normalise(tx);
            tx = activity.categorise(tx);
            activity.save(tx);
            saveCount ++;
        }
        return saveCount;
    }
}


