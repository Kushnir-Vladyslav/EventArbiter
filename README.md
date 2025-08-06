# EventArbiter

EventArbiter is a Java library that provides a robust and flexible event management system built around a central singleton manager that ensures efficient event distribution between publishers and subscribers.

## Key Features

### Architecture
- Central EventManager (singleton) for event distribution management
- Support for synchronous and asynchronous operations
- Built-in event priority system
- Thread-safe implementation of all components
- Automatic cleanup of completed events
- Component state management system (CREATED, RUNNING, PAUSED, STOPPED, SHUTDOWN)

### Flexibility and Extensibility
- Custom publisher creation using the EventPublisher abstract class
- Custom subscriber creation using the EventSubscriber abstract class
- Ready-to-use implementations for common use cases

### Built-in Implementations

#### Publishers
Synchronous:
- `SyncEventPublisher` - basic synchronous publisher
- `ConditionalSyncEventPublisher` - synchronous publisher that checks for compliance with conditions
- `TimeoutEventPublisher` - synchronous publisher with timeout cancellation

Asynchronous:
- `AsyncEventPublisher` - basic asynchronous publisher
- `BatchEventPublisher` - accumulates events and asynchronously publishes them all at once
- `ConditionalAsyncEventPublisher` - asynchronous publisher that checks for compliance with conditions
- `DelayedEventPublisher` - asynchronously publishes an event with a delay
- `PeriodicEventPublisher` - asynchronously periodically publishes an event
- `SilentTimeoutEventPublisher` - asynchronous publisher with silent timeout cancellation

#### Subscribers
Base subscribers:
- `SyncEventSubscriber` - basic subscriber with on-demand event processing
- `AsyncEventSubscriber` - asynchronous subscriber with separate processing thread
- `BatchEventSubscriber` - asynchronous subscriber for batch event processing
- `BufferedEventSubscriber` - subscriber that returns events without processing

## Installation

```xml
<dependency>
    <groupId>io.github.kushnir-vladyslav</groupId>
    <artifactId>event-arbiter</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Project Structure

```
src/
├── main/java/io/github/kushnirvladyslav/
│   ├── Event.java                    # Base event class
│   ├── EventManager.java             # Central event manager
│   ├── EventPublisher.java           # Base publisher abstract class
│   ├── EventSubscriber.java          # Base subscriber abstract class
│   ├── EventPriority.java            # Priority enumeration
│   ├── Status.java                   # Status enumeration
│   ├── ControlledListFuture.java     # Future management utility
│   │
│   ├── EventPublishers/              # Publisher implementations
│   │   ├── AsyncEventPublisher.java
│   │   ├── SyncEventPublisher.java
│   │   ├── ExecuteEventPublisher.java
│   │   ├── ConditionalSyncEventPublisher.java
│   │   ├── TimeoutEventPublisher.java
│   │   └── SilentTimeoutEventPublisher.java
│   ├── EventSubscribers/             # Subscriber implementations
│   │   ├── SyncEventSubscriber.java
│   │   ├── AsyncEventSubscriber.java
│   │   ├── BatchEventSubscriber.java
│   │   └── BufferedEventSubscriber.java
```

## Component States

Components (publishers and subscribers) have the following states:
- `CREATED` - initial state after creation
- `RUNNING` - active state, processing events
- `PAUSED` - temporary stop with state preservation
    - For subscribers: events published during pause will be missed
    - For publishers: attempting to publish will throw an exception
- `STOPPED` - complete stop with state reset
    - When restarted, component begins work as new
    - All accumulated data is cleared
- `SHUTDOWN` - final stop
    - Cannot be restarted
    - New instance must be created
    - All resources are released

## Usage Examples

### Automatic Resource Management

```java
try (AsyncEventPublisher<String> publisher = new AsyncEventPublisher<>();
     AsyncEventSubscriber subscriber = new AsyncEventSubscriber(true)) { // auto-start

    subscriber.subscribeEvent(StringEvent.class, (StringEvent e) -> { ... } );
    publisher.publish(new StringEvent<>("Hello, World!"));
    
} // Automatic shutdown of both components
```

### Manual Lifecycle Management

```java
AsyncEventPublisher<String> publisher = new AsyncEventPublisher<>();
AsyncEventSubscriber subscriber = new AsyncEventSubscriber(); // no auto-start

// Subscribe
subscriber.subscribeEvent(StringEvent.class, (StringEvent e) -> { ... } );

// Manual subscriber start
subscriber.run();

// Publish events
publisher.publish(new StringEvent<>("Event 1"));

// Pause subscriber - next event will be missed
subscriber.pause();
publisher.publish(new StringEvent<>("Will be missed"));

// Resume operation
subscriber.run();
publisher.publish(new StringEvent<>("Event 2"));

// Stop with state reset
subscriber.stop();
// After stopping the subscriber, the state will be completely reset, including subscriptions.

// Restart - begins as new
subscriber.run();

// Next event will be missed
publisher.publish(new StringEvent<>("Will be missed"));

// Mandatory resource cleanup
subscriber.shutdown();
publisher.shutdown();

```

### Creating Custom Publisher

```java
public class CustomEventPublisher extends EventPublisher {
    
    protected void process(Event<?> event, ...) {
        // Custom processing logic
        publishEvent(event);
    }
}
```

#### Available API of the basic EventPublisher

##### `protected void publishEvent(Event<?> event)`
- Publishes the event to the event management system
- Checks for null and publisher status
- Throws an error if the event is null or the publisher is stopped

##### `public Status getStatus()`
- Returns the current status of the publisher's work

##### `protected void setStatus(Status newStatus)`
- Set the status of the publisher's work

##### `public boolean isRunning()`
##### `public boolean isShutdown()`
- Check if the publisher is in this state

##### `protected void checkNotShutdown()`
- Check if the publisher is shutdown, if true throw exception

### Creating Custom Subscriber

```java
public class CustomEventSubscriber extends EventSubscriber {
    public CustomEventSubscriber(boolean autoStart) {
        if (autoStart) {
            run();
        }
    }

    @Override
    public void run() {
        checkNotShutdown();
        // Custom preparation logic
        subscribe();
        setStatus(Status.RUNNING);
    }
    
    protected void processEvent(...) {
        try {
            Event<?> event = subscriberQueue.take();
            // Custom processing logic
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            break;
        }
    }

    @Override
    public void pause() {
        checkNotShutdown();

        if (isRunning()) {
            setStatus(Status.PAUSED);
            unsubscribe();
            // Custom logic
        }
    }

    @Override
    public void stop() {
        checkNotShutdown();

        if (isRunning() || isPaused()) {
            // Custom cleaning logic
            super.stop();
        }
    }

    @Override
    public void shutdown() {
        checkNotShutdown();
        
        unsubscribe();
        // Custom destroy logic
        setStatus(Status.SHUTDOWN);
    }
}
```

#### Available API of the basic EventSubscriber

##### `protected final PriorityBlockingQueue<Event<?>> subscriberQueue;`
-  Queue for storing received events before processing

##### `protected void subscribe()`
##### `protected void unsubscribe()`
- Subscribe/unsubscribe a listener from the event manager

##### `public Status getStatus()`
- Returns the current status of the publisher's work

##### `protected void setStatus(Status newStatus)`
- Set the status of the publisher's work

##### `public boolean isRunning()`
##### `public boolean isPaused()`
##### `public boolean isStopped()`
##### `public boolean isShutdown()`
##### `protected boolean canRun()`
- Check if the publisher is in this state

##### `protected void clearQueue()`
- Clears all pending events from the subscriber's queue

##### `protected void checkNotShutdown()`
- Check if the publisher is shutdown, if true throw exception

## Important Usage Notes

### Lifecycle Management

- **EventManager**:
    - Automatically created and started on first use
    - Manual state management needed only in special cases
    - Instance access: `EventManager.getInstance()`

- **Publishers/Subscribers**:
    - Mandatory shutdown via `shutdown()` or try-with-resources
    - Use system resources that must be released
    - Have state indicator (`getStatus()`)
    - Support pause and resume operations

## Requirements

- Java 8 or higher
- SLF4J for logging

## License

Apache License 2.0

## Author

Vladyslav Kushnir - [GitHub Profile](https://github.com/Kushnir-Vladyslav)