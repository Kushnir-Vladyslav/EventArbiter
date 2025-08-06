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

package io.github.kushnirvladyslav.Events;

import io.github.kushnirvladyslav.Event;

import java.util.List;

/**
 * A specialized event type that contains a list of events of a specific type.
 * This class allows for batch processing of multiple events of the same type.
 *
 * @param <T> the type of events contained in the list, must extend Event
 *
 * @author Vladyslav Kushnir
 * @version 1.0
 * @since 2025-07-31
 * @see Event
 */
public class ListEvents <T extends Event<?>> extends Event<List<T>> {
    /** The class object representing the type of events in the list */
    private final Class<T> eventType;

    /**
     * Creates a new ListEvents instance with the specified event type and list of events.
     *
     * @param eventType the class object representing the type of events in the list
     * @param events the list of events to be contained in this ListEvents
     */
    public ListEvents (Class<T> eventType, List<T> events) {
        this.eventType = eventType;
        data = events;
    }

    /**
     * Returns the class object representing the type of events in the list.
     *
     * @return the event type class
     */
    public Class<T> getEventType() {
        return eventType;
    }
}
