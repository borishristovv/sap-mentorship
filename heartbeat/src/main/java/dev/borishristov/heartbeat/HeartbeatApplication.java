
package dev.borishristov.heartbeat;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.beans.factory.annotation.Value;

import java.io.PrintStream;

@SpringBootApplication
@EnableScheduling
public class HeartbeatApplication {

    public static void main(String[] args) {
        SpringApplication.run(HeartbeatApplication.class, args);
    }

    @Bean(destroyMethod = "")
    PrintStream outputStream() {
        return System.out;
    }

    @Bean
    CommandLineRunner runChecker(WebsiteChecker checker,
                                 PrintStream out,
                                 @Value("${heartbeat.monitor.enabled:false}") boolean monitoringEnabled) {
        return args -> {
            if (monitoringEnabled) {
                return;
            }

            if (args.length != 1) {
                out.println("Usage: heartbeat <url>");
                return;
            }

            CheckResult result = checker.check(args[0]);

            out.println("Website: " + result.url());
            out.println("Status: " + result.status());

            if (result.error() != null) {
                out.println("Error: " + result.error());
            } else {
                out.println("HTTP Status: " + result.httpStatus());
                out.println("Response Time: " + result.responseTimeMs() + " ms");
            }
        };
    }
}
