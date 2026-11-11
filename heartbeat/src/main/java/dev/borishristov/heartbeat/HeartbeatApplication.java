
package dev.borishristov.heartbeat;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.beans.factory.annotation.Value;

@SpringBootApplication
@EnableScheduling
public class HeartbeatApplication {

    public static void main(String[] args) {
        SpringApplication.run(HeartbeatApplication.class, args);
    }

    @Bean
    CommandLineRunner runChecker(WebsiteChecker checker,
                                 @Value("${heartbeat.monitor.enabled:false}") boolean monitoringEnabled) {
        return args -> {
            if (monitoringEnabled) {
                return;
            }

            if (args.length != 1) {
                System.out.println("Usage: heartbeat <url>");
                return;
            }

            CheckResult result = checker.check(args[0]);

            System.out.println("Website: " + result.url());
            System.out.println("Status: " + result.status());

            if (result.error() != null) {
                System.out.println("Error: " + result.error());
            } else {
                System.out.println("HTTP Status: " + result.httpStatus());
                System.out.println("Response Time: " + result.responseTimeMs() + " ms");
            }
        };
    }
}
