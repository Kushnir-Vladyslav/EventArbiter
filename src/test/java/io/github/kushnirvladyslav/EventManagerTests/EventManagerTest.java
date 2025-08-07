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

import io.github.kushnirvladyslav.EventManagerTests.TestEventSubscriber.MockEventSubscriber;
import io.github.kushnirvladyslav.EventManager;
import io.github.kushnirvladyslav.EventSubscriber;
import io.github.kushnirvladyslav.Status;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventManagerTest {
    private EventManager eventManager;

    @BeforeEach
    void setUp() {
        eventManager = EventManager.getInstance();

        if (eventManager.getStatus() != Status.RUNNING) {
            eventManager.run();
        }
    }

    @Test
    void testSingletonInstance() {
        EventManager anotherInstance = EventManager.getInstance();
        assertSame(eventManager, anotherInstance);
    }

    @Test
    void testInitialState() {
        assertEquals(Status.RUNNING, eventManager.getStatus());
        assertEquals(0, eventManager.getSubscriberCount());
        assertEquals(0, eventManager.getQueueSize());
    }

    @Test
    void testStatusTransitions() {
        // RUNNING -> PAUSED
        eventManager.pause();
        assertEquals(Status.PAUSED, eventManager.getStatus());

        // PAUSED -> RUNNING
        eventManager.run();
        assertEquals(Status.RUNNING, eventManager.getStatus());

        // RUNNING -> STOPPED
        eventManager.stop();
        assertEquals(Status.STOPPED, eventManager.getStatus());

        // STOPPED -> RUNNING
        eventManager.run();
        assertEquals(Status.RUNNING, eventManager.getStatus());
    }

    @Test
    void testPauseResume() {
        assertEquals(Status.RUNNING, eventManager.getStatus());

        assertDoesNotThrow(() -> eventManager.pause());
        assertEquals(Status.PAUSED, eventManager.getStatus());

        assertDoesNotThrow(() -> eventManager.run());
        assertEquals(Status.RUNNING, eventManager.getStatus());
    }

    @Test
    void testInvalidStateTransitions() {
        eventManager.stop();
        assertEquals(Status.STOPPED, eventManager.getStatus());

        assertDoesNotThrow(() -> eventManager.pause());
    }

    @Test
    void testMultipleStarts() {
        assertDoesNotThrow(() -> {
            eventManager.run();
            eventManager.run();
            eventManager.run();
        });
        assertEquals(Status.RUNNING, eventManager.getStatus());
    }

    @Test
    void testMultiplePauses() {
        eventManager.run();

        assertDoesNotThrow(() -> {
            eventManager.pause();
            eventManager.pause();
            eventManager.pause();
        });
        assertEquals(Status.PAUSED, eventManager.getStatus());
    }

    @Test
    void testMultipleStops() {
        assertDoesNotThrow(() -> {
            eventManager.stop();
            eventManager.stop();
            eventManager.stop();
        });
        assertEquals(Status.STOPPED, eventManager.getStatus());
    }

    @Test
    void testPublishNullEvent() {
        assertThrows(IllegalArgumentException.class, () -> eventManager.publish(null));
    }

    @Test
    void testSubscribeNullSubscriber() {
        assertThrows(IllegalArgumentException.class, () -> eventManager.subscribe(null));
    }

    @Test
    void testUnsubscribeNullSubscriber() {
        assertThrows(IllegalArgumentException.class, () -> eventManager.unsubscribe(null));
    }

    @Test
    void testSubscriberCountManagement() {
        assertEquals(0, eventManager.getSubscriberCount());

        EventSubscriber subscriber1 = new MockEventSubscriber();
        EventSubscriber subscriber2 = new MockEventSubscriber();

        eventManager.subscribe(subscriber1);
        assertEquals(1, eventManager.getSubscriberCount());

        eventManager.subscribe(subscriber2);
        assertEquals(2, eventManager.getSubscriberCount());

        eventManager.unsubscribe(subscriber1);
        assertEquals(1, eventManager.getSubscriberCount());

        eventManager.unsubscribe(subscriber2);
        assertEquals(0, eventManager.getSubscriberCount());
    }

    @AfterEach
    void tearDown() {
        if (eventManager.getStatus() != Status.RUNNING) {
            eventManager.run();
        }
    }
}
