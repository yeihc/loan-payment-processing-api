package com.yeihc.application.event;

import com.yeihc.domain.event.DomainEvent;
import java.util.List;

/**
 * Output Port responsible for propagating domain events to external consumers.
 * * DESIGN DECISIONS:
 * 1. Pull Model Implementation: Works in tandem with the 'Aggregate Root Pull Model',
 * where the use case extracts events from entities and hands them to this dispatcher.
 * 2. Decoupling: Abstracts the messaging infrastructure (Spring Events, Kafka, RabbitMQ)
 * from the Application Logic.
 * 3. Reliability: Implementations of this interface should consider transactional
 * consistency (e.g., only dispatching events after the DB transaction commits).
 */
public interface DomainEventDispatcher {

    /**
     * Publishes a collection of domain events to all registered subscribers.
     * * @param events A list of events collected from one or more Aggregate Roots
     * during the execution of a Use Case.
     */
    void dispatch(List<DomainEvent> events);
}