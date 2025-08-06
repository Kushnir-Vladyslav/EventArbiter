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

/**
 * Functional interface for handling errors that occur during event processing.
 * Provides a mechanism for custom error handling strategies.
 *
 * @author Vladyslav Kushnir
 * @version 1.0
 * @since 2025-07-31
 * @see Event
 */
@FunctionalInterface
public interface EventErrorHandler {
    /**
     * Handles an error that occurred during event processing.
     *
     * @param event the event that caused the error
     * @param e the exception that occurred
     */
    void handle(Event<?> event, Exception e);
}
