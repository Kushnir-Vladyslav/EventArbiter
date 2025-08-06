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

package io.github.kushnirvladyslav.EventManagerTests;

import io.github.kushnirvladyslav.EventManager;
import io.github.kushnirvladyslav.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EventManagerShutdownTest {
    private EventManager eventManager;

    @BeforeEach
    void setUp() {
        eventManager = EventManager.getInstance();

        if (eventManager.getStatus() != Status.RUNNING) {
            eventManager.run();
        }
    }

    @Test
    void testShutdownPreventsRestart() {
        eventManager.shutdown();
        assertEquals(Status.SHUTDOWN, eventManager.getStatus());

        assertThrows(IllegalStateException.class, () -> eventManager.run());
        assertThrows(IllegalStateException.class, () -> eventManager.pause());
        assertThrows(IllegalStateException.class, () -> eventManager.stop());

        assertThrows(IllegalStateException.class, () -> eventManager.shutdown());
    }
}
