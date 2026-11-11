
package dev.borishristov.heartbeat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(
        name = "heartbeat.monitor.enabled",
        havingValue = "true"
)
public class WebsiteMonitor {

    private final WebsiteChecker checker;
    private final List<String> urls;

    public WebsiteMonitor(
            WebsiteChecker checker,
            @Value("${heartbeat.monitor.urls}") List<String> urls
    ) {
        this.checker = checker;
        this.urls = urls;
    }

    @Scheduled(fixedDelayString = "${heartbeat.monitor.interval-ms}", initialDelayString = "${heartbeat.monitor.initial-delay-ms:0}")
    public void checkWebsites() {
        for (String url : urls) {
            CheckResult result = checker.check(url);

            System.out.println("Website: " + result.url());
            System.out.println("Status: " + result.status());

            if (result.error() != null) {
                System.out.println("Error: " + result.error());
            } else {
                System.out.println("HTTP Status: " + result.httpStatus());
                System.out.println("Response Time: " + result.responseTimeMs() + " ms");
            }

            System.out.println();
        }
    }
}
