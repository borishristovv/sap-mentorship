package dev.borishristov.heartbeat;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface MonitorRepository extends MongoRepository<Monitor, String> {
}