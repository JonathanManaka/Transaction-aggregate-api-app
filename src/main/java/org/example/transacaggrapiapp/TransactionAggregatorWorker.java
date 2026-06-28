package org.example.transacaggrapiapp;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.example.transacaggrapiapp.TransactionAggregatorActivity.TransactionAggregatorActivityImpl;
import org.example.transacaggrapiapp.aggregator.TransactionAggregator;
import org.example.transacaggrapiapp.aggregator.TransactionNormaliser;
import org.example.transacaggrapiapp.categoriser.TransactionCategoriser;
import org.example.transacaggrapiapp.repository.TransactionRepository;
import org.example.transacaggrapiapp.source.MockBankAClient;
import org.example.transacaggrapiapp.source.MockBankBClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "temporal.worker.enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
public class TransactionAggregatorWorker {

    @Value("${temporal.service-address}")
    private String temporalHost;

    private final MockBankAClient bankAClient;
    private final MockBankBClient bankBClient;
    private final TransactionNormaliser normaliser;
    private final TransactionCategoriser categoriser;
    private final TransactionRepository transactionRepository;

    @PostConstruct
    public void start() {
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

        WorkerFactory factory = WorkerFactory.newInstance(client);
        Worker worker = factory.newWorker(Shared.TRANSACTION_AGGREGATOR_TASK_QUEUE);

        worker.registerWorkflowImplementationTypes(TransactionAggregator.class);
        worker.registerActivitiesImplementations(
                new TransactionAggregatorActivityImpl(
                        bankAClient, bankBClient, normaliser, categoriser, transactionRepository
                )
        );

        Thread workerThread = new Thread(() -> {
            int attempt = 0;
            while (true) {
                try {
                    factory.start();
                    System.out.println("Temporal worker started successfully");
                    return;
                } catch (Exception e) {
                    attempt++;
                    long backoff = Math.min(30_000L, attempt * 5_000L);
                    System.err.println("Failed to start Temporal worker (attempt " + attempt + "), retrying in " + (backoff / 1000) + "s: " + e.getMessage());
                    try {
                        Thread.sleep(backoff);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        });
        workerThread.setDaemon(true);
        workerThread.setName("temporal-worker-starter");
        workerThread.start();
    }
}
