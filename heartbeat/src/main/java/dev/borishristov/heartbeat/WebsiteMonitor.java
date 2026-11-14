
package dev.borishristov.heartbeat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.PrintStream;
import java.util.List;


/**
 * Periodically checks the configured websites when monitoring is enabled.
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

    public WebsiteMonitor(
            WebsiteChecker checker,
            @Value("${heartbeat.monitor.urls}") List<String> urls,
            PrintStream out
    ) {
        this.checker = checker;
        this.urls = urls;
        this.out = out;
    }

    /**
     * Checks each configured website and prints its result.
     * The next execution is scheduled after the configured fixed delay.
     */
    @Scheduled(
            fixedDelayString = "${heartbeat.monitor.interval}",
            initialDelayString = "${heartbeat.monitor.initial-delay:0s}"
    )
    public void checkWebsites() {
        for (String url : urls) {
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
}
