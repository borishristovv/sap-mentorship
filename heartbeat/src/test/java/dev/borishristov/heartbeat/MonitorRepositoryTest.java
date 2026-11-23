
package dev.borishristov.heartbeat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MonitorRepositoryTest {

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
