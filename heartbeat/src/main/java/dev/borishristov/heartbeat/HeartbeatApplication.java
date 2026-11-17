
package dev.borishristov.heartbeat;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;

import java.io.PrintStream;
import java.util.List;
import java.util.Optional;

@SpringBootApplication
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
                                 MonitorRepository repository,
                                 PrintStream out,
                                 @Value("${heartbeat.monitor.enabled:false}") boolean monitoringEnabled) {

        return args -> {
            if (monitoringEnabled) {
                return;
            }

            if (args.length == 1 && args[0].equals("list")) {
                List<Monitor> monitors = repository.findByEnabledTrue();

                if (monitors.isEmpty()) {
                    out.println("No monitored websites found.");
                    return;
                }

                for (Monitor monitor : monitors) {
                    out.println("ID: " + monitor.getId());
                    out.println("URL: " + monitor.getUrl());
                    out.println("Enabled: " + monitor.isEnabled());
                    out.println();
                }

                return;
            }

            if (args.length == 2 && args[0].equals("remove")) {
                String id = args[1];

                Optional<Monitor> existing = repository.findById(id);

                if (existing.isEmpty()) {
                    out.println("Monitor not found: " + id);
                    return;
                }

                Monitor monitor = existing.get();
                monitor.setEnabled(false);
                repository.save(monitor);

                out.println("Monitor deactivated successfully!");
                out.println("ID: " + id);
                return;
            }

            // Add a new website to MongoDB
            if (args.length == 2 && args[0].equals("add")) {
                String url = args[1];

                try {
                    java.net.URI uri = java.net.URI.create(url);

                    if (!("http".equalsIgnoreCase(uri.getScheme())
                            || "https".equalsIgnoreCase(uri.getScheme()))
                            || uri.getHost() == null) {
                        out.println("Invalid website URL.");
                        return;
                    }
                } catch (IllegalArgumentException e) {
                    out.println("Invalid website URL.");
                    return;
                }

                Optional<Monitor> existing = repository.findByUrl(url);

                if (existing.isPresent()) {
                    Monitor monitor = existing.get();

                    if (monitor.isEnabled()) {
                        out.println("Website is already being monitored.");
                        out.println("ID: " + monitor.getId());
                        return;
                    }

                    monitor.setEnabled(true);
                    repository.save(monitor);

                    out.println("Website reactivated successfully!");
                    out.println("ID: " + monitor.getId());
                    return;
                }

                Monitor monitor = new Monitor(url, true);
                Monitor saved = repository.save(monitor);

                out.println("Website added successfully!");
                out.println("ID: " + saved.getId());
                out.println("URL: " + saved.getUrl());
                return;
            }

            // Existing single-website checking functionality
            if (args.length != 1) {
                out.println("Usage:");
                out.println("  heartbeat <url>");
                out.println("  heartbeat add <url>");
                out.println("  heartbeat list");
                out.println("  heartbeat remove <id>");
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
