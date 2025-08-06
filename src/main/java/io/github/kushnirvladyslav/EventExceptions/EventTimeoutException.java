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

package io.github.kushnirvladyslav.EventExceptions;

/**
 * Exception thrown when an event execution exceeds its timeout period.
 */
public class EventTimeoutException extends RuntimeException {

    public EventTimeoutException(String message) {
        super(message);
    }

    public EventTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
