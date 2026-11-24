
package dev.borishristov.heartbeat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.testcontainers.mongodb.MongoDBContainer;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(MonitorRepositoryTest.MongoTestConfig.class)
class MonitorRepositoryTest {

    @TestConfiguration(proxyBeanMethods = false)
    static class MongoTestConfig {

        @Bean
        @ServiceConnection
        MongoDBContainer mongoDBContainer() {
            return new MongoDBContainer("mongo:8.0");
        }
    }

    @Autowired
    private MonitorRepository repository;

    @Test
    void shouldSaveAndRetrieveMonitor() {
        Monitor monitor = new Monitor("https://example.com");

        Monitor saved = repository.save(monitor);

        try {
            assertNotNull(saved.getId());

            Monitor retrieved = repository.findById(saved.getId())
                    .orElseThrow();

            assertEquals("https://example.com", retrieved.getUrl());
        } finally {
            repository.deleteById(saved.getId());
        }
    }
}
