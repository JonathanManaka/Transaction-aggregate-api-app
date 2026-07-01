package org.example.transacaggrapiapp;

import io.temporal.client.WorkflowOptions;
import io.temporal.client.schedules.Schedule;
import io.temporal.client.schedules.ScheduleActionStartWorkflow;
import io.temporal.client.schedules.ScheduleAlreadyRunningException;
import io.temporal.client.schedules.ScheduleClient;
import io.temporal.client.schedules.ScheduleClientOptions;
import io.temporal.client.schedules.ScheduleIntervalSpec;
import io.temporal.api.enums.v1.ScheduleOverlapPolicy;
import io.temporal.client.schedules.ScheduleOptions;
import io.temporal.client.schedules.SchedulePolicy;
import io.temporal.client.schedules.ScheduleSpec;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.example.transacaggrapiapp.transactionAggregatorWorkflow.TransactionAggregatorWorkflow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/**
 * Creates a Temporal Schedule that starts the aggregation workflow every 5 minutes.
 * The schedule is created idempotently on startup; overlapping runs are skipped so a
 * slow aggregation never stacks on the next tick.
 */
@Component
@ConditionalOnProperty(name = "temporal.schedule.enabled", havingValue = "true", matchIfMissing = false)
@Slf4j
public class TransactionAggregatorSchedule {

    private static final String SCHEDULE_ID = "aggregator-every-5min";
    private static final String WORKFLOW_ID = "aggregator-scheduled";

    @Value("${temporal.service-address}")
    private String temporalHost;

    @PostConstruct
    public void register() {
        Thread thread = new Thread(this::createScheduleWithRetry);
        thread.setDaemon(true);
        thread.setName("temporal-schedule-creator");
        thread.start();
    }

    private void createScheduleWithRetry() {
        int attempt = 0;
        while (true) {
            try {
                createSchedule();
                return;
            } catch (ScheduleAlreadyRunningException e) {
                log.info("Temporal schedule '{}' already exists, skipping creation.", SCHEDULE_ID);
                return;
            } catch (Exception e) {
                attempt++;
                long backoff = Math.min(30_000L, attempt * 5_000L);
                log.warn("Failed to create Temporal schedule (attempt {}), retrying in {}s: {}",
                        attempt, backoff / 1000, e.getMessage());
                try {
                    Thread.sleep(backoff);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    private void createSchedule() {
        WorkflowServiceStubs service = WorkflowServiceStubs.newServiceStubs(
                WorkflowServiceStubsOptions.newBuilder()
                        .setTarget(temporalHost)
                        .build()
        );

        ScheduleClient scheduleClient = ScheduleClient.newInstance(
                service,
                ScheduleClientOptions.newBuilder()
                        .setNamespace("default")
                        .build()
        );

        Schedule schedule = Schedule.newBuilder()
                .setAction(ScheduleActionStartWorkflow.newBuilder()
                        .setWorkflowType(TransactionAggregatorWorkflow.class)
                        .setOptions(WorkflowOptions.newBuilder()
                                .setWorkflowId(WORKFLOW_ID)
                                .setTaskQueue(Shared.TRANSACTION_AGGREGATOR_TASK_QUEUE)
                                .build())
                        .build())
                .setSpec(ScheduleSpec.newBuilder()
                        .setIntervals(List.of(new ScheduleIntervalSpec(Duration.ofMinutes(5))))
                        .build())
                .setPolicy(SchedulePolicy.newBuilder()
                        .setOverlap(ScheduleOverlapPolicy.SCHEDULE_OVERLAP_POLICY_SKIP)
                        .build())
                .build();

        scheduleClient.createSchedule(SCHEDULE_ID, schedule, ScheduleOptions.newBuilder().build());
        log.info("Created Temporal schedule '{}' (every 5 minutes).", SCHEDULE_ID);
    }
}
