package com.yeihc.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Base contract for all Domain Events.
 * * DESIGN PRINCIPLES:
 * 1. Historical Record: Events represent immutable facts that happened in the past.
 * 2. Semantic Naming: Use past tense for implementations (e.g., AccountOpened).
 * 3. Pull Model: Aggregates collect these events, and the Application Service
 * dispatches them after a successful database transaction.
 */
public interface DomainEvent {

    /**
     * Unique identifier for this specific event instance.
     * Crucial for idempotency and tracking (Distributed Tracing).
     */
    default UUID eventId() {
        return UUID.randomUUID();
    }

    /**
     * The timestamp of when the business event actually occurred in UTC.
     */
    Instant occurredAt();

    /**
     * The ID of the Aggregate (e.g., Account ID) that produced this event.
     */
    UUID aggregateId();

    /**
     * Categorizes events to allow listeners to filter by aggregate type.
     * e.g., "Account", "Transfer"
     */
    default String aggregateType() {
        return "Unknown";
    }

    /**
     * Technical name for the event, used for serialization and logging.
     * Default implementation uses the class name.
     */
    default String type() {
        return this.getClass().getSimpleName();
    }
}