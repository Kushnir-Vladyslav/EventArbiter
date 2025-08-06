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

import io.github.kushnirvladyslav.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;

/**
 * Asynchronous event publisher that filters events based on a condition.
 * Extends {@link AsyncEventPublisher} to provide conditional event publishing.
 *
 * <p>This publisher:
 * <ul>
 * <li>Evaluates each event against a predicate before publishing</li>
 * <li>Allows dynamic updates of the filtering condition</li>
 * <li>Maintains asynchronous publishing behavior from parent class</li>
 * </ul>
 *
 * @author Vladyslav Kushnir
 * @version 1.0
 * @since 2025-07-31
 * @see AsyncEventPublisher
 */
public class ConditionalAsyncEventPublisher extends AsyncEventPublisher {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConditionalAsyncEventPublisher.class);

    /** Predicate used to filter events */
    private Predicate<Event<?>> condition;

    /**
     * Creates a new ConditionalAsyncEventPublisher with specified filtering condition.
     *
     * @param condition the predicate to test events against
     * @throws IllegalArgumentException if condition is null
     */
    public ConditionalAsyncEventPublisher(Predicate<Event<?>> condition) {
        super();
        if (condition == null) {
            LOGGER.error("Attempted to create publisher with null condition");
            throw new IllegalArgumentException("Condition cannot be null");
        }
        this.condition = condition;
        LOGGER.debug("Created ConditionalAsyncEventPublisher");
    }

    /**
     * Updates the filtering condition used to evaluate events.
     *
     * @param condition the new predicate to test events against
     * @throws IllegalArgumentException if condition is null
     * @throws IllegalStateException if publisher is shut down
     */
    public void setCondition(Predicate<Event<?>> condition) {
        if (condition == null) {
            LOGGER.error("Attempted to set null condition");
            throw new IllegalArgumentException("Condition cannot be null");
        }

        checkNotShutdown();

        LOGGER.debug("Updating event filtering condition");
        this.condition = condition;
    }

    /**
     * Publishes an event if it passes the filtering condition.
     *
     * @param event the event to evaluate and potentially publish
     * @throws IllegalArgumentException if event is null
     */
    public void publish(Event<?> event) {
        if (event == null) {
            LOGGER.error("Attempted to publish null event");
            throw new IllegalArgumentException("Event cannot be null");
        }

        if (condition.test(event)) {
            LOGGER.debug("Event {} passed condition, publishing", event.getClass().getSimpleName());
            super.publish(event);
        } else {
            LOGGER.debug("Event {} filtered out by condition", event.getClass().getSimpleName());
        }
    }
}
