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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Abstract base class for event publishers that use ExecutorService.
 * Provides shared timeUnit field for subclasses and handles executor shutdown.
 *
 * <p>Subclasses must initialize the executor field in their constructor.
 * The executor should never be null or changed during the object's lifetime.
 *
 * <p>Note: This class performs immediate shutdown via shutdownNow().
 * Responsibility for graceful task completion lies with the executor users.
 *
 * @author Vladyslav Kushnir
 * @version 1.0
 * @since 2025-06-11
 * @see EventPublisher
 * @see ExecutorService
 */
public abstract class ExecuteEventPublisher extends EventPublisher{
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecuteEventPublisher.class);
    /** ExecutorService for handling asynchronous operations */
    protected ExecutorService executor;
    /** Shared TimeUnit for subclass task scheduling */
    protected TimeUnit timeUnit;

    /**
     * Shuts down this publisher and its ExecutorService.
     * Performs immediate shutdown of the executor using shutdownNow().
     *
     * @throws IllegalStateException if the executor was not properly initialized
     * @throws IllegalStateException if this publisher is already shut down
     */
    @Override
    public void shutdown() {
        checkNotShutdown();

        if (executor == null) {
            String message = "Executor is null in " + this.getClass().getSimpleName() +
                    " - this indicates a programming error in constructor initialization";
            LOGGER.error(message);
            throw new IllegalStateException(message);
        }


        LOGGER.info("Shutting down ExecuteEventPublisher: {}", this.getClass().getSimpleName());

        executor.shutdownNow();
        LOGGER.debug("Executor shutdown completed for {}", this.getClass().getSimpleName());


        setStatus(Status.SHUTDOWN);
    }
}
