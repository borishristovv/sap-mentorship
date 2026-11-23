
package dev.borishristov.heartbeat;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.concurrent.*;

import java.io.PrintStream;
import java.time.Duration;
import java.util.List;

/**
 * Periodically monitors websites using a dedicated scheduling thread.
 * Retrieves monitors from MongoDB and checks websites concurrently
 * using a fixed thread pool.
 */
@Component
@ConditionalOnProperty(
        name = "heartbeat.monitor.enabled",
        havingValue = "true"
)
public class WebsiteMonitor {

    private final WebsiteChecker checker;
    private final MonitorRepository repository;
    private final PrintStream out;
    private final Duration interval;
    private final Duration initialDelay;

    private volatile boolean running = false; // Ensures changes to running are visible across threads
    private Thread worker;
    private final ExecutorService executor = Executors.newFixedThreadPool(3);

    public WebsiteMonitor(
            WebsiteChecker checker,
            MonitorRepository repository,
            PrintStream out,
            @Value("${heartbeat.monitor.interval}") Duration interval,
            @Value("${heartbeat.monitor.initial-delay:0s}") Duration initialDelay
    ) {
        this.checker = checker;
        this.repository = repository;
        this.out = out;
        this.interval = interval;
        this.initialDelay = initialDelay;
    }

    /**
     * Starts the monitoring thread after Spring Boot is ready.
     * Prevents multiple monitoring loops from starting on the same instance.
     */
    @EventListener(ApplicationReadyEvent.class)
    public synchronized void startMonitoring() {
        if (running) {
            return;
        }

        if (interval.isZero() || interval.isNegative()
                || initialDelay.isNegative()) {
            throw new IllegalArgumentException(
                    "Invalid monitoring interval or initial delay"
            );
        }

        running = true;

        worker = new Thread(this::monitorLoop, "heartbeat-monitor");
        worker.start();
    }

    private void monitorLoop() {
        try {
            Thread.sleep(initialDelay);

            while (running && !Thread.currentThread().isInterrupted()) {
                checkWebsites();

                if (running) {
                    Thread.sleep(interval);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            running = false;
        }
    }


    /**
     * Retrieves monitored websites from MongoDB and submits HTTP checks
     * to the executor for parallel execution.
     * Waits for the results and prints them to the console.
     */
    public void checkWebsites() {
        List<Future<CheckResult>> futures = new ArrayList<>();

        for (Monitor monitor : repository.findAll()) {
            if (Thread.currentThread().isInterrupted()) {
                break;
            }

            futures.add(executor.submit(
                    () -> checker.check(monitor.getUrl())
            ));
        }

        for (Future<CheckResult> future : futures) {
            try {
                CheckResult result = future.get();

                out.println("Website: " + result.url());
                out.println("Status: " + result.status());

                if (result.error() != null) {
                    out.println("Error: " + result.error());
                } else {
                    out.println("HTTP Status: " + result.httpStatus());
                    out.println("Response Time: "
                            + result.responseTimeMs() + " ms");
                }

                out.println();

            } catch (InterruptedException e) {
                for (Future<CheckResult> pending : futures) {
                    pending.cancel(true);
                }

                Thread.currentThread().interrupt();
                return;

            } catch (ExecutionException e) {
                out.println("Website check failed: " + e.getCause());
            }
        }
    }


    /**
     * Stops periodic monitoring during application shutdown.
     * Interrupts the monitoring thread, cancels executor tasks,
     * and waits for termination.
     */
    @PreDestroy
    public synchronized void stopMonitoring() {
        running = false;

        if (worker != null) {
            worker.interrupt();
        }

        executor.shutdownNow();

        try {
            if (worker != null) {
                worker.join(5000); // Wait up to 5 seconds for the monitoring thread to terminate
            }

            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                out.println("Warning: Executor did not terminate within 5 seconds.");
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
