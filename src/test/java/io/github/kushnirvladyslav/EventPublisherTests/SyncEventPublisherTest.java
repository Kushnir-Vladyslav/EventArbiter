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

package io.github.kushnirvladyslav.EventPublisherTests;

import io.github.kushnirvladyslav.TestsEvents.DoubleEventTest;
import io.github.kushnirvladyslav.TestsEvents.IntEventTest;
import io.github.kushnirvladyslav.TestsEvents.StringEventTest;
import io.github.kushnirvladyslav.EventManager;
import io.github.kushnirvladyslav.EventPriority;
import io.github.kushnirvladyslav.EventPublishers.SyncEventPublisher;
import io.github.kushnirvladyslav.EventSubscribers.SyncEventSubscriber;
import io.github.kushnirvladyslav.Status;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class SyncEventPublisherTest {
    private SyncEventPublisher publisher;
    private SyncEventSubscriber subscriber;
    private final EventManager eventManager = EventManager.getInstance();

    @BeforeEach
    void setUp() {
        publisher = new SyncEventPublisher();
        subscriber = new SyncEventSubscriber();
    }

    @Test
    void testPublisherInitialStatus() {
        assertEquals(Status.RUNNING, publisher.getStatus());
    }

    @Test
    void testPublishSingleEvent() throws InterruptedException {
        StringEventTest event = new StringEventTest("test message");
        final AtomicInteger handlerCount = new AtomicInteger(0);
        final AtomicReference<String> receivedData = new AtomicReference<>();

        subscriber.subscribeEvent(StringEventTest.class, (StringEventTest e) -> {
            handlerCount.incrementAndGet();
            receivedData.set(e.getData());
        });
        subscriber.run();

        publisher.publish(event);
        Thread.sleep(50);
        subscriber.processEvents();

        assertEquals(1, handlerCount.get());
        assertEquals("test message", receivedData.get());
    }

    @Test
    void testPublishMultipleEvents() throws InterruptedException {
        final AtomicInteger stringCount = new AtomicInteger(0);
        final AtomicInteger intCount = new AtomicInteger(0);
        final AtomicInteger doubleCount = new AtomicInteger(0);

        subscriber.subscribeEvent(StringEventTest.class, e -> stringCount.incrementAndGet());
        subscriber.subscribeEvent(IntEventTest.class, e -> intCount.incrementAndGet());
        subscriber.subscribeEvent(DoubleEventTest.class, e -> doubleCount.incrementAndGet());
        subscriber.run();

        publisher.publish(new StringEventTest("test1"));
        publisher.publish(new StringEventTest("test2"));
        publisher.publish(new IntEventTest(42));
        publisher.publish(new DoubleEventTest(3.14));
        publisher.publish(new StringEventTest("test3"));

        Thread.sleep(50);

        subscriber.processEvents();

        assertEquals(3, stringCount.get());
        assertEquals(1, intCount.get());
        assertEquals(1, doubleCount.get());
    }

    @Test
    void testPublishEventWithPriority() throws InterruptedException {
        StringBuilder processOrder = new StringBuilder();

        subscriber.subscribeEvent(StringEventTest.class, (StringEventTest event) ->
                processOrder.append(event.getData()));
        subscriber.run();

        publisher.publish(new StringEventTest("L", EventPriority.LOW));
        publisher.publish(new StringEventTest("H", EventPriority.HIGH));
        publisher.publish(new StringEventTest("M", EventPriority.MEDIUM));

        Thread.sleep(50);

        subscriber.processEvents();

        assertEquals("HML", processOrder.toString());
    }

    @Test
    void testPublishToMultipleSubscribers() {
        SyncEventSubscriber subscriber2 = new SyncEventSubscriber();
        final AtomicInteger subscriber1Count = new AtomicInteger(0);
        final AtomicInteger subscriber2Count = new AtomicInteger(0);

        try {
            subscriber.subscribeEvent(StringEventTest.class, e -> subscriber1Count.incrementAndGet());
            subscriber2.subscribeEvent(StringEventTest.class, e -> subscriber2Count.incrementAndGet());

            subscriber.run();
            subscriber2.run();

            StringEventTest event = new StringEventTest("broadcast test");
            publisher.publish(event);

            Thread.sleep(50);
            subscriber.processEvents();
            subscriber2.processEvents();

            assertEquals(1, subscriber1Count.get());
            assertEquals(1, subscriber2Count.get());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            subscriber2.shutdown();
        }
    }

    @Test
    void testPublishWithNoSubscribers() {
        StringEventTest event = new StringEventTest("no subscribers");

        assertDoesNotThrow(() -> publisher.publish(event));
    }

    @Test
    void testPublishNullEvent() throws InterruptedException {
        subscriber.subscribeEvent(StringEventTest.class, e -> {});
        subscriber.run();

        assertThrows(IllegalArgumentException.class, () -> publisher.publish(null));
        Thread.sleep(50);
        assertDoesNotThrow(() -> subscriber.processEvents());
    }

    @Test
    void testPublishEventData() throws InterruptedException {
        StringEventTest stringEvent = new StringEventTest("string data");
        IntEventTest intEvent = new IntEventTest(123);
        DoubleEventTest doubleEvent = new DoubleEventTest(45.67);

        final AtomicReference<String> stringData = new AtomicReference<>();
        final AtomicReference<Integer> intData = new AtomicReference<>();
        final AtomicReference<Double> doubleData = new AtomicReference<>();

        subscriber.subscribeEvent(StringEventTest.class, e -> stringData.set(e.getData()));
        subscriber.subscribeEvent(IntEventTest.class, e -> intData.set(e.getData()));
        subscriber.subscribeEvent(DoubleEventTest.class, e -> doubleData.set(e.getData()));
        subscriber.run();

        publisher.publish(stringEvent);
        publisher.publish(intEvent);
        publisher.publish(doubleEvent);

        Thread.sleep(50);

        subscriber.processEvents();

        assertEquals("string data", stringData.get());
        assertEquals(Integer.valueOf(123), intData.get());
        assertEquals(Double.valueOf(45.67), doubleData.get());
    }

    @Test
    void testPublishEventTimestamp() throws InterruptedException {
        final AtomicLong eventTimestamp = new AtomicLong(0);

        subscriber.subscribeEvent(StringEventTest.class, e ->
                eventTimestamp.set(e.getCreatedTimeMillis()));
        subscriber.run();

        long beforePublish = System.currentTimeMillis();
        publisher.publish(new StringEventTest("timestamp test"));
        long afterPublish = System.currentTimeMillis();

        Thread.sleep(50);

        subscriber.processEvents();

        long timestamp = eventTimestamp.get();
        assertTrue(timestamp >= beforePublish && timestamp <= afterPublish);
    }

    @Test
    void testPublishLargeNumberOfEvents() throws InterruptedException {
        final AtomicInteger eventCount = new AtomicInteger(0);
        final int totalEvents = 1000;

        subscriber.subscribeEvent(IntEventTest.class, e -> eventCount.incrementAndGet());
        subscriber.run();

        for (int i = 0; i < totalEvents; i++) {
            publisher.publish(new IntEventTest(i));
        }

        Thread.sleep(50);

        subscriber.processEvents();

        assertEquals(totalEvents, eventCount.get());
    }

    @Test
    void testPublishAfterSubscriberShutdown() throws InterruptedException {
        final AtomicInteger handlerCount = new AtomicInteger(0);

        subscriber.subscribeEvent(StringEventTest.class, e -> handlerCount.incrementAndGet());
        subscriber.run();

        publisher.publish(new StringEventTest("before shutdown"));
        Thread.sleep(50);
        subscriber.processEvents();
        assertEquals(1, handlerCount.get());

        subscriber.shutdown();

        publisher.publish(new StringEventTest("after shutdown"));
        Thread.sleep(50);
        assertEquals(1, handlerCount.get()); // Count should remain the same
    }

    @Test
    void testPublisherShutdown() {
        assertNotEquals(Status.SHUTDOWN, publisher.getStatus());

        assertDoesNotThrow(() -> publisher.shutdown());
        assertEquals(Status.SHUTDOWN, publisher.getStatus());

        assertThrows(IllegalStateException.class, () -> publisher.shutdown());
    }

    @Test
    void testPublishAfterPublisherShutdown() throws InterruptedException {
        final AtomicInteger handlerCount = new AtomicInteger(0);
        subscriber.subscribeEvent(StringEventTest.class, e -> handlerCount.incrementAndGet());
        subscriber.run();

        publisher.shutdown();

        assertThrows(IllegalStateException.class, () -> publisher.publish(new StringEventTest("after publisher shutdown")));
        Thread.sleep(50);
        subscriber.processEvents();

        assertEquals(0, handlerCount.get());
    }

    @Test
    void testEventIntegrity() throws InterruptedException {
        final List<StringEventTest> receivedEvents = new ArrayList<>();

        subscriber.subscribeEvent(StringEventTest.class, e -> receivedEvents.add(e));
        subscriber.run();

        StringEventTest originalEvent = new StringEventTest("integrity test");
        publisher.publish(originalEvent);
        Thread.sleep(50);
        subscriber.processEvents();

        assertEquals(1, receivedEvents.size());
        StringEventTest receivedEvent = receivedEvents.get(0);

        assertEquals(originalEvent.getData(), receivedEvent.getData());
        assertEquals(originalEvent.getPriority(), receivedEvent.getPriority());
        assertEquals(originalEvent.getCreatedTimeMillis(), receivedEvent.getCreatedTimeMillis());
    }

    @Test
    void testPublishEventWithErrorInSubscriber() throws InterruptedException {
        final AtomicInteger successCount = new AtomicInteger(0);
        final AtomicInteger errorCount = new AtomicInteger(0);

        subscriber.subscribeEvent(StringEventTest.class,
                (StringEventTest event) -> {
                    if ("error".equals(event.getData())) {
                        throw new RuntimeException("Test error in subscriber");
                    } else {
                        successCount.incrementAndGet();
                    }
                },
                (event, exception) -> errorCount.incrementAndGet()
        );
        subscriber.run();

        publisher.publish(new StringEventTest("success"));
        Thread.sleep(50);
        subscriber.processEvents();

        publisher.publish(new StringEventTest("error"));
        Thread.sleep(50);
        subscriber.processEvents();

        publisher.publish(new StringEventTest("success2"));
        Thread.sleep(50);
        subscriber.processEvents();

        assertEquals(2, successCount.get());
        assertEquals(1, errorCount.get());
        assertEquals(1, subscriber.getTotalErrorCount());
    }

    @Test
    void testPublishOrderWithSamePriority() throws InterruptedException {
        final List<Integer> processedOrder = new ArrayList<>();
        final Object lock = new Object();

        subscriber.subscribeEvent(IntEventTest.class, e -> {
            synchronized (lock) {
                processedOrder.add(e.getData());
            }
        });
        subscriber.run();

        for (int i = 0; i < 10; i++) {
            publisher.publish(new IntEventTest(i));
        }

        Thread.sleep(50);

        subscriber.processEvents();

        assertEquals(10, processedOrder.size());
        Set<Integer> expectedValues = new HashSet<>();
        Set<Integer> actualValues = new HashSet<>(processedOrder);
        for (int i = 0; i < 10; i++) {
            expectedValues.add(i);
        }
        assertEquals(expectedValues, actualValues);
    }

    @Test
    void testPublishDifferentEventInstances() throws InterruptedException {
        final AtomicInteger eventCount = new AtomicInteger(0);
        final Set<StringEventTest> receivedEvents = new HashSet<>();

        subscriber.subscribeEvent(StringEventTest.class, e -> {
            eventCount.incrementAndGet();
            receivedEvents.add(e);
        });
        subscriber.run();

        publisher.publish(new StringEventTest("same data"));
        publisher.publish(new StringEventTest("same data"));
        publisher.publish(new StringEventTest("same data"));

        Thread.sleep(50);

        subscriber.processEvents();

        assertEquals(3, eventCount.get());
        assertEquals(3, receivedEvents.size());
    }

    @AfterEach
    void tearDown() {
        if (subscriber != null && subscriber.getStatus() != Status.SHUTDOWN) {
            subscriber.shutdown();
        }
        if (publisher != null && publisher.getStatus() != Status.SHUTDOWN) {
            publisher.shutdown();
        }
    }
}
