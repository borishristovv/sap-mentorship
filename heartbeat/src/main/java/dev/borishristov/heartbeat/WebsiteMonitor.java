
package dev.borishristov.heartbeat;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.PrintStream;
import java.time.Duration;
import java.util.List;

/**
 * Periodically monitors websites using a dedicated Java thread.
 */
@Component
@ConditionalOnProperty(
        name = "heartbeat.monitor.enabled",
        havingValue = "true"
)
public class WebsiteMonitor {

    private final WebsiteChecker checker;
    private final List<String> urls;
    private final PrintStream out;
    private final Duration interval;
    private final Duration initialDelay;

    private volatile boolean running = false;
    private Thread worker;

    public WebsiteMonitor(
            WebsiteChecker checker,
            @Value("${heartbeat.monitor.urls}") List<String> urls,
            PrintStream out,
            @Value("${heartbeat.monitor.interval}") Duration interval,
            @Value("${heartbeat.monitor.initial-delay:0s}") Duration initialDelay
    ) {
        this.checker = checker;
        this.urls = urls;
        this.out = out;
        this.interval = interval;
        this.initialDelay = initialDelay;
    }

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

    public void checkWebsites() {
        for (String url : urls) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            CheckResult result = checker.check(url);

            out.println("Website: " + result.url());
            out.println("Status: " + result.status());

            if (result.error() != null) {
                out.println("Error: " + result.error());
            } else {
                out.println("HTTP Status: " + result.httpStatus());
                out.println("Response Time: " + result.responseTimeMs() + " ms");
            }

            out.println();
        }
    }

    @PreDestroy
    public synchronized void stopMonitoring() {
        running = false;

        if (worker != null) {
            worker.interrupt();

            try {
                worker.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
