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

package io.github.kushnirvladyslav;

import io.github.kushnirvladyslav.Events.ListEvents;

/**
 * Functional interface for handling lists of events.
 * Specializes {@link BaseEventHandler} for {@link ListEvents} type events.
 *
 * @param <T> the type of events in the list, must extend Event
 *
 * @author Vladyslav Kushnir
 * @version 1.0
 * @since 2025-07-31
 * @see BaseEventHandler
 * @see ListEvents
 */
@FunctionalInterface
public interface ListEventsHandler<T extends Event<?>> extends BaseEventHandler<ListEvents<T>> {
}
