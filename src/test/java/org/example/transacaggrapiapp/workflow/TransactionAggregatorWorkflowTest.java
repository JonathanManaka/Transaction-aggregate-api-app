package org.example.transacaggrapiapp.workflow;

import io.temporal.client.WorkflowOptions;
import io.temporal.testing.TestWorkflowRule;
import org.example.transacaggrapiapp.TransactionAggregatorActivity.TransactionAggregatorActivity;
import org.example.transacaggrapiapp.TransactionAggregatorActivity.TransactionAggregatorActivityImpl;
import org.example.transacaggrapiapp.aggregator.TransactionNormaliser;
import org.example.transacaggrapiapp.categoriser.TransactionCategoriser;
import org.example.transacaggrapiapp.model.Category;
import org.example.transacaggrapiapp.model.Transaction;
import org.example.transacaggrapiapp.repository.TransactionRepository;
import org.example.transacaggrapiapp.source.MockBankAClient;
import org.example.transacaggrapiapp.source.MockBankBClient;
import org.example.transacaggrapiapp.transactionAggregatorWorkflow.TransactionAggregatorWorkflow;
import org.example.transacaggrapiapp.aggregator.TransactionAggregator;
import org.junit.Rule;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class TransactionAggregatorWorkflowTest {

    @Rule
    public TestWorkflowRule testWorkflowRule =
            TestWorkflowRule.newBuilder()
                    .setWorkflowTypes(TransactionAggregator.class)
                    .setDoNotStart(true)
                    .build();

    @Test
    public void testIntegrationAggregate() {
        MockBankAClient bankAClient = mock(MockBankAClient.class);
        MockBankBClient bankBClient = mock(MockBankBClient.class);
        TransactionRepository repository = mock(TransactionRepository.class);
        TransactionNormaliser normaliser = new TransactionNormaliser();
        TransactionCategoriser categoriser = new TransactionCategoriser();

        List<Transaction> bankATransactions = List.of(
                transaction("A-001", "BANK_A", "Woolworths Food Purchase", new BigDecimal("450.00"))
        );
        List<Transaction> bankBTransactions = List.of(
                transaction("B-001", "BANK_B", "Netflix Monthly Subscription", new BigDecimal("199.00"))
        );

        when(bankAClient.fetchTransactions()).thenReturn(bankATransactions);
        when(bankBClient.fetchTransactions()).thenReturn(bankBTransactions);
        when(repository.findByExternalIdAndSource(anyString(), anyString())).thenReturn(Optional.empty());

        testWorkflowRule.getWorker().registerActivitiesImplementations(
                new TransactionAggregatorActivityImpl(bankAClient, bankBClient, normaliser, categoriser, repository)
        );
        testWorkflowRule.getTestEnvironment().start();

        TransactionAggregatorWorkflow workflow = testWorkflowRule
                .getWorkflowClient()
                .newWorkflowStub(
                        TransactionAggregatorWorkflow.class,
                        WorkflowOptions.newBuilder()
                                .setTaskQueue(testWorkflowRule.getTaskQueue())
                                .build()
                );

        int saved = workflow.aggregate();

        assertEquals(2, saved);
        testWorkflowRule.getTestEnvironment().shutdown();
    }

    @Test
    public void testMockedAggregate() {
        TransactionAggregatorActivity activity = mock(TransactionAggregatorActivity.class, withSettings().withoutAnnotations());

        List<Transaction> transactions = List.of(
                transaction("A-001", "BANK_A", "Woolworths Food Purchase", new BigDecimal("450.00"))
        );

        when(activity.fetchBankATransactions()).thenReturn(transactions);
        when(activity.fetchBankBTransactions()).thenReturn(List.of());
        when(activity.isPresent(any(Transaction.class))).thenReturn(false);

        testWorkflowRule.getWorker().registerActivitiesImplementations(activity);
        testWorkflowRule.getTestEnvironment().start();

        TransactionAggregatorWorkflow workflow = testWorkflowRule
                .getWorkflowClient()
                .newWorkflowStub(
                        TransactionAggregatorWorkflow.class,
                        WorkflowOptions.newBuilder()
                                .setTaskQueue(testWorkflowRule.getTaskQueue())
                                .build()
                );

        int saved = workflow.aggregate();

        assertEquals(1, saved);
        verify(activity).normalise(any(Transaction.class));
        verify(activity).categorise(any(Transaction.class));
        verify(activity).save(any(Transaction.class));
        testWorkflowRule.getTestEnvironment().shutdown();
    }

    @Test
    public void testDuplicatesAreSkipped() {
        TransactionAggregatorActivity activity = mock(TransactionAggregatorActivity.class, withSettings().withoutAnnotations());

        List<Transaction> transactions = List.of(
                transaction("A-001", "BANK_A", "Woolworths Food Purchase", new BigDecimal("450.00"))
        );

        when(activity.fetchBankATransactions()).thenReturn(transactions);
        when(activity.fetchBankBTransactions()).thenReturn(List.of());
        when(activity.isPresent(any(Transaction.class))).thenReturn(true);

        testWorkflowRule.getWorker().registerActivitiesImplementations(activity);
        testWorkflowRule.getTestEnvironment().start();

        TransactionAggregatorWorkflow workflow = testWorkflowRule
                .getWorkflowClient()
                .newWorkflowStub(
                        TransactionAggregatorWorkflow.class,
                        WorkflowOptions.newBuilder()
                                .setTaskQueue(testWorkflowRule.getTaskQueue())
                                .build()
                );

        int saved = workflow.aggregate();

        assertEquals(0, saved);
        verify(activity, never()).normalise(any(Transaction.class));
        verify(activity, never()).categorise(any(Transaction.class));
        verify(activity, never()).save(any(Transaction.class));
        testWorkflowRule.getTestEnvironment().shutdown();
    }

    private Transaction transaction(String externalId, String source, String description, BigDecimal amount) {
        return Transaction.builder()
                .externalId(externalId)
                .source(source)
                .description(description)
                .amount(amount)
                .currency("ZAR")
                .category(Category.OTHER)
                .transactionDate(LocalDateTime.now())
                .accountId("ACC-001")
                .build();
    }
}
