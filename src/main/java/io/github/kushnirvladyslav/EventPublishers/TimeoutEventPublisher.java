/*
 *   Copyright 2025 Kushnir Vladyslav
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.github.kushnirvladyslav.EventPublishers;

import io.github.kushnirvladyslav.ControlledListFuture;
import io.github.kushnirvladyslav.Event;
import io.github.kushnirvladyslav.EventExceptions.EventTimeoutException;
import io.github.kushnirvladyslav.ExecuteEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Event publisher that executes events with timeout constraints, throwing an exception on timeout.
 * Extends {@link ExecuteEventPublisher} to provide timeout-controlled event publishing capabilities.
 *
 * <p>This publisher:
 * <ul>
 * <li>Executes events with specified timeout periods</li>
 * <li>Throws {@link EventTimeoutException} when events exceed their timeout</li>
 * <li>Uses {@link ControlledListFuture} for managing futures</li>
 * <li>Provides cleanup of completed and cancelled tasks</li>
 * </ul>
 *
 * <p>Unlike {@link SilentTimeoutEventPublisher}, this publisher explicitly notifies about timeouts through exceptions.
 *
 * @author Vladyslav Kushnir
 * @version 1.0
 * @since 2025-07-31
 * @see ExecuteEventPublisher
 * @see ControlledListFuture
 * @see EventTimeoutException
 */
public class TimeoutEventPublisher extends ExecuteEventPublisher {
    private static final Logger LOGGER = LoggerFactory.getLogger(TimeoutEventPublisher.class);

    /** Controls and tracks futures */
    private final ControlledListFuture listFuture;

    /**
     * Creates a new TimeoutEventPublisher with specified time unit.
     *
     * @param timeUnit the default time unit for timeouts
     * @throws IllegalArgumentException if timeUnit is null
     */
    public TimeoutEventPublisher (TimeUnit timeUnit) {
        super();
        if (timeUnit == null) {
            LOGGER.error("Attempted to create publisher with null TimeUnit");
            throw new IllegalArgumentException("TimeUnit cannot be null");
        }

        this.executor = Executors.newFixedThreadPool(1);
        this.listFuture = new ControlledListFuture();
        this.timeUnit = timeUnit;

        LOGGER.debug("Created TimeoutEventPublisher with timeUnit={}", timeUnit);
    }

    /**
     * Creates a new TimeoutEventPublisher with milliseconds as default time unit.
     */
    public TimeoutEventPublisher () {
        this(TimeUnit.MILLISECONDS);
    }

    /**
     * Publishes an event with a timeout constraint.
     * Throws an exception if the event execution exceeds the timeout.
     *
     * @param event the event to publish
     * @param timeout the maximum time to wait for event completion
     * @param timeUnit the time unit of the timeout
     * @throws IllegalArgumentException if event is null, timeout is negative, or timeUnit is null
     * @throws IllegalStateException if publisher is shut down
     * @throws EventTimeoutException if event execution exceeds the timeout
     * @throws RuntimeException if event execution fails for any other reason
     */
    public void publish(Event<?> event, long timeout, TimeUnit timeUnit) {
        if (event == null) {
            LOGGER.error("Attempted to publish null event");
            throw new IllegalArgumentException("Event cannot be null");
        }
        if (timeout < 0) {
            LOGGER.error("Attempted to publish with negative timeout: {}", timeout);
            throw new IllegalArgumentException("Timeout cannot be negative");
        }
        if (timeUnit == null) {
            LOGGER.error("Attempted to publish with null TimeUnit");
            throw new IllegalArgumentException("TimeUnit cannot be null");
        }

        checkNotShutdown();

        Future<?> future = executor.submit(() -> publishEvent(event));
        listFuture.add(future);
        try {
            future.get(timeout, timeUnit);
        } catch (TimeoutException e) {
            future.cancel(true);
            LOGGER.error("Event {} exceeded timeout of {} {}",
                    event.getClass().getSimpleName(),
                    timeout,
                    timeUnit);
            throw new EventTimeoutException("Event execution exceeded timeout of " +
                    timeout + " " + timeUnit, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Event {} was interrupted", event.getClass().getSimpleName());
            throw new RuntimeException("Event execution was interrupted", e);
        } catch (Exception e) {
            LOGGER.error("Event {} failed with error: {}",
                    event.getClass().getSimpleName(),
                    e.getCause().getMessage());
            throw new RuntimeException("Event execution failed", e.getCause());
        }
    }

    /**
     * Publishes an event with a timeout constraint using the default time unit.
     *
     * @param event the event to publish
     * @param timeout the maximum time to wait for event completion
     * @throws IllegalArgumentException if event is null or timeout is negative
     * @throws IllegalStateException if publisher is shut down
     * @throws EventTimeoutException if event execution exceeds the timeout
     * @throws RuntimeException if event execution fails for any other reason
     */
    public void publish(Event<?> event, long timeout) {
        publish(event, timeout, timeUnit);
    }

    /**
     * Gets the number of currently executing events.
     *
     * @return number of pending events
     * @throws IllegalStateException if publisher is shut down
     */
    public int getPendingEventsCount() {
        checkNotShutdown();
        return listFuture.getFutures().size();
    }

    /**
     * Cancels all pending events.
     *
     * @return number of cancelled events
     * @throws IllegalStateException if publisher is shut down
     */
    public int cancelAllPendingEvents() {
        checkNotShutdown();

        int count = getPendingEventsCount();
        if (count > 0) {
            LOGGER.info("Cancelling {} pending events", count);
            listFuture.stopAll();
        }

        return count;
    }

    /**
     * Shuts down the publisher and cancels all pending events.
     *
     * @throws IllegalStateException if already shut down
     */
    @Override
    public void shutdown() {
        checkNotShutdown();

        int pendingCount = getPendingEventsCount();
        if (pendingCount > 0) {
            LOGGER.info("Shutting down with {} pending events", pendingCount);
        }

        listFuture.stopControlAndShutdown();
        super.shutdown();
    }
}
