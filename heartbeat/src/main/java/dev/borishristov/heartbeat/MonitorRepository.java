package dev.borishristov.heartbeat;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;
import java.util.List;

public interface MonitorRepository extends MongoRepository<Monitor, String> {

    Optional<Monitor> findByUrl(String url);

    List<Monitor> findByEnabledTrue();
}