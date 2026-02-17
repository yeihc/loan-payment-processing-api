package com.yeihc.infrastructure.event;

import com.yeihc.application.event.DomainEventDispatcher;
import com.yeihc.domain.event.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Infrastructure Adapter that implements DomainEventDispatcher by logging events.
 * * DESIGN DECISIONS:
 * 1. Observability: Provides immediate visibility into domain changes during
 * development and debugging without external dependencies.
 * 2. Non-Blocking Intent: Currently synchronous, making it easy to follow the
 * execution stack in the application logs.
 * 3. Strategy Pattern: This implementation can be easily replaced or augmented
 * by more complex dispatchers (e.g., SpringEvents, RabbitMQ) using Spring Profiles.
 */
@Component
public class LoggingDomainEventDispatcher implements DomainEventDispatcher {

    private static final Logger log = LoggerFactory.getLogger(LoggingDomainEventDispatcher.class);

    /**
     * Iterates through the provided list and logs the technical metadata of each event.
     * * @param events List of DomainEvents pulled from the Aggregate Roots.
     */
    @Override
    public void dispatch(List<DomainEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }

        for (DomainEvent event : events) {
            // Using structured logging format for better log aggregation (like ELK/Splunk)
            log.info("DomainEvent dispatched: type={}, aggregateId={}, occurredAt={}",
                    event.type(),
                    event.aggregateId(),
                    event.occurredAt());
        }
    }
}