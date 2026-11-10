
package dev.borishristov.heartbeat;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class HeartbeatApplication {

    public static void main(String[] args) {
        SpringApplication.run(HeartbeatApplication.class, args);
    }

    @Bean
    CommandLineRunner runChecker() {
        return args -> {
            if (args.length != 1) {
                System.out.println("Usage: heartbeat <url>");
                return;
            }

            WebsiteChecker checker = new WebsiteChecker();
            CheckResult result = checker.check(args[0]);

            System.out.println("Website: " + result.url());

            if (result.error() != null) {
                System.out.println("Status: DOWN");
                System.out.println("Error: " + result.error());
            } else {
                System.out.println("HTTP Status: " + result.httpStatus());
                System.out.println("Response Time: " + result.responseTimeMs() + " ms");
            }
        };
    }
}
