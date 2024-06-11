# Event store using JPA

This is Spring Boot starter that uses DB to store events. It uses Spring Boot JPA.

The example is in domain of icecream truck that is explained in [list of videos on Youtube](https://www.youtube.com/playlist?list=PL-nSd-yeckKh7Ts5EKChek7iXcgyUGDHa) by The Dev Owl.

This project is starter and in example is project that is using thi starter. In order to build example you need to run docker compose which startes the Postgres DB:

```sh
docker-compose -f docker-compose-db.yml up
```

## Terms and functionality

### Event or domain event

- events are stored in event store
- events represent what happened in the past and it ce not be changed
- basic data in event:
  - `id` - the key in the database, has no meaning, if it is in memory then we don't need it
  - `stream id` (or `aggregate id`) - the identifier of a stream, by which we extract events from the event store
  - `version` - represents the ordinal number and is always an increasing number. It can also be useful for transactions because `stream id` and `version` must be unique in the DB table
  - `content` (payload or data) - data of the event
  - `meta data` - data related to the event, e.g. who made it and when, which command is the cause of this event (to achieve idempotency)
  - `event type` and `type version` - this is handy to have if we want to do filtering by event type. The other thing is if some event type change over time. For example, events with different data are needed. Since they mean the same thing, then by version we can know what data we have and how to use it. E.g. order created event in version 1 had customer which content is first and last name and in version 2 we split it to first name and last name. So both versions have the same meaning but the data is different. In the program we need to know how to extract the data. That is the reason why we have versions.

### Commands

A command is something sent by the user. It is a trigger for some action that will result in an event. It is used for writing. The client does not write directly to the store. The client sends commands which represents the client's intention. Usually the commands are sent to the command handler, i.e. the aggregate which handles command.

### View or report

Represents the state that is presented to the user. It is generated from a series of events (projected).

## Building blocks

### Aggregate - write model

Aggregate is term from the DDD (Domain Driven Design).

Each aggregate has its own stream of events. It is built from a series of events (it has an internal projection - evolve/poject function). This is also called rehydration.

The main purpose of the aggregate is to recreate the state from events so that the execution of the command can be validated.

It receives commands and processes them (write):

- `command + aggregate state = new events` - decide/handle function
- after generating the event, it can calculate the new state
- `new event + aggregate state = new aggregate state` - project/evolve function
- commands can be rejected - when rejected, what was rejected can be recorded (new event) if it is necessary. E.g. for an audit or for something else.

We want to execute the command over the aggregate. In order to do this, the aggregate must know its condition (state). So the steps for handling command are following:

- we load all the events of an aggregate from event store (usually executed by service)
- We create an aggregate object (usually executed by service)
- we give loaded events to the constructor of the aggregate ((usually executed by service)
- after that we send aggregate a command for processing (service call handle function in aggregate)
- the result is either the command executed or not (executed by aggregate)
- when processing the command, business rules are used (executed by aggregate)
- when the command is executed, the result is a new event that must be stored in the store (usually executed by service)

*Command handler function*:

- receives commands and executes them
- checks the conditions
- commands can be rejected - when rejected, what was rejected can be recorded (new event) if it is necessary for an audit or for something else
- if the command is accepted, then it generates an event that is saved in store

Design: the aggregate can be requested to write new events in the event store or it can return new events that someone else(e.g. service) will save in the store.

Aggregates should have a fixed life, for example, events related to purchases are not in the customer aggregate, but in a special aggregate that has a reference to the customer.

If we have too many events in the aggregate and there is problem of long processing, we can do snapshots - the command handler loads the snapshot and then all the events after the snapshot. Every x events we can make a new snapshot - that's technical optimization. The question is do we need them? Performance should be measured.

### Event store

It saves events (memory, database, disk, ...). Events can only be added to the end, i.e. no deletion. Events can be retrieved in the following way:

- all events
- filtering according to some criteria, for example:
  - by stream id - returns all events in the stream
  - by stream id and version - returns all events from version (including)
  - by type of event (there may be more then one type) - convenient when creating reports
  - ...

### Query

It is used for reading (*read model* - *view* or *report*).

Event store is not good for retrieving the last state. We do that with the query handler.

Query handler can subscribe to the event stream and when events arrive, it uses projection to generate the current state. When query need to be processed it just read from the current model in memory.

The other approach is to process event stream on demand when some query arrives. So in that case the query handler reads the event stream and run projection on each event. The result is projected state which is then used to generate query result.

So the formula is `event + read model = updated read model'.

State generated by projection don't have to be saved (it can because of performance).

Projected stete can always be built from events.

We usually have many projections and each one is optimized for one query.

A projector is a process that creates a projection:

- each projector has a version of the stream it reached
- it only retrieves events from that version
- and make a projection over them
- it can work periodically, and it can also be subscribed to events or work upon query
- it can be in another process or distributed

Clients do not use events directly, but send queries to handlers that execute the queries.

Projectors do not share state. Projections are cheap and easy to make.

When the data in the state needs to be changed, the projection is simply deleted and re-generated with a new projection from events.

Introducing a change in the projection. Let's make a new projection that has a change:

- we perform both projections (old and new) in the system
- let's check that the new projection works well
- when we want, we can switch projection clients to a new projection
- after that, we turn off the old one if no one is using it

### Reactor

It is used for everything except read and write.

The `trigger` of the reactor can be an event or some moment (*schedule*).

The result can be:

- call of an external system (API *call*)
- new command towards the another component/aggregate

Reactors process the event stream, but do not create projections that are used for queries.

They react to events, i.e. they result in a reaction, for example, the initiation of external behavior or the creation of other events that are placed in the event store or both.

Example: reactor is subscribed to all orders completed events and can have logic that will declare a customer a gold customer and store that event in the store.

Another example: when a customer becomes golden, send them an email with instructions and then generate an event about it.

At startup, they regenerate the internal state as well as the aggregate. They do not trigger external behavior when recreating the state from history.

Sagas are implemented in reactors.

## Testing

We use GWT (given when then).

### Aggregate

Given:

- previous domain events with important details

When:

- command is handled

Then:

- domain event is generated
- aggregate state is updated

Errors:

- command is rejected
- check conditions (error message), aggregate state, generated domain events
- check that command validation has not passed

### View/report

Given:

- previous domain events with important details

When:

- projection is started
  - request from client or
  - event subscription

Then:

- generated read model - projected data

Errors:

- read model can have error - in that case event could be skipped (it depends on use case). In the case it could be logical to have partial results for user then not have results at all. We can log this situation for later analysis
- we can always delete read model, fix error and recreate read model from events.

They are eventually consistent. They can be in memory.

If recreation is long because of large number of events we can do snapshots. And update read model from snapshot.

### Reactor

Given:

- previous domain events with relevant details and/or
- some view stete

When:

- domain event - called handler

Then:

- generated command with details (new domain event) or
- API call sent

Errors:

- if the error is due to business rules then a domain event is generated
  - the read model can listen to it and show it to the user indirectly
  - the reactor can listen to it and can take corrective action
- if it is a technical error (e.g. null pointer exception, ...)
  - we try to repeat the execution
  - if it's a temporary problem (e.g. we're disconnected from the network) that's ok
  - we can try some repetition strategies with backoff policy
  - if it is a permanent problem, it is a bug, then the reactor is blocked
  - we need to see individually how to solve it
  - dilemma - skip the event or block yourself

## Some remarks

### At least once delivery -> duplicates

The aggregate must be able to recognize duplicates and correct itself.

For example need to see how to implement command idempotency. -> Each command can have its own unique id. After processing the command, the event that is created can have a command id (in meta data). When validating the command, it is possible to check which command was processed and which was not.

### Side effects

We want to record the event in the database (*event store*) and send a request over the network (pub/sub).

Outbox pattern:

- save the event and request/command to the database. That's in one transaction.
- another process/reactor periodically reads requests and executes them
  - if he can't, he will try next time
  - if it can, it will delete the request from the database

This is how spring modulith works.

### Correction of data

When the user makes a mistake. There should be a corrective action (compensatory event).

### Event versions

When there are errors in the data, we can:

- do event migration
  - can be easily done if we can stop the application
  - if we can't then strangler fig pattern can be used

The scheme of events will change, but we can't simply change them in the store. There are following solutions:

- we can have handlers that know how to process old and new events
- we can read old events and transform them into new ones and then process them (proxy for transformation when reading from the store)
- we can upgrade the entire store

### Privacy

Privacy and sensitive data in the store are problematic.

We should not store such things in stores because we cannot change stores.

Or we should save them in a different way, for example, we save passwords in the database outside of store, and in store we have reference to password in database.

### Distribution of events

Question:

 > I see. How would you handle *subscriptions*? I mean catch-up subscriptions in evenstoredb

Answer:

> Either you *poll* (which is a bit harder to handle).
> - polling is often suitable for processors, but even there I use it very rarely. which does not mean it does not work, just not how I do it
> I use a push mechanism and notify everyone interested.
> - whenever an event happens its propagated to interested subscribers
>

### Eventual consistency

The real world is also eventually consistent:

- We cannot eliminate it, we can only manage it
- The risk increases with time as some part is not consistent
- Trashhold of inconsistencies depends on the context
  - order - seconds are ok
  - Monthly sales report - hours are ok
- Possible consistency management
  - users should be educated, i.e. inform them how long they will see a change in the system
  - unsubscribe to news letter - you will be removed from the list within 7 days
  - we can show what was written, i.e. after sending the order, we can show the user what we wrote down, even though it hasn't been processed yet
  - slow down the user - spinner
  - speed ​​up the backend

## Advantages and disadvantages

It's not mainstream:

- There is little literature on it
- There are not enough frameworks and libraries
- It is difficult to find people who know how to do it
- Without a lot of knowledge, there will be a lot of problems
- J curve productivity

![](https://miro.medium.com/v2/resize:fit:1358/0*5OPkNSHy98-Bs0Wy)

Photo from: https://miro.medium.com/v2/resize:fit:1358/0*5OPkNSHy98-Bs0Wy

- Complexity
  - complexity comes with connecting components as with microservices
  - It is difficult to connect how something is executed
  - It is easy to make a complex connection of complex components which is the worst possible implementation
  - should have a complex connection of simple components
  - many developers will have problems with this

If it is done well, it gives us great opportunities, but if it is done badly, it is worse than the worst ball of mud.

## Implementation

### package `hr.fer.event`

Event is in record `Event`:

```java
public record Event<D>(
    StreamId streamId,
    int version, // version inside streamId
    
    String eventType,
    int eventTypeVersion,
    
    D eventData,
    Map<String,String> metaData) {

  public Event<D> copyWithVersion(int newVersion) {
    return new Event<>(streamId, newVersion, eventType, eventTypeVersion, eventData, metaData);
  }

  public static <D> List<Event<D>> createList(StreamId id, String eventType, int eventTypeVersion,  List<D> data,  Map<String,String> metaData) {
    return data.stream()
        .map(d -> new Event<>(id, -1, eventType, eventTypeVersion, d, metaData))
        .toList();
  }

  public static <D> Event<D> of(StreamId id, String eventType, int eventTypeVersion, D data, Map<String,String> metaData) {
    return new Event<>(id, -1, eventType, eventTypeVersion, data, metaData);
  }
}
```

We have  `StreamId` which has segments. Last segment can be  random (TSID):

```java
public final class StreamId implements Iterable<String> {
  private List<String> segments;

  private StreamId(String ...segments) {
    this(Arrays.asList(segments).stream()
        .filter(s -> !s.isEmpty())
        .toList());
  }

  private StreamId(List<String> segments) {
    for(var s: segments)
      if(s.contains("-"))
        throw new IllegalArgumentException("Segments can not have '-'.");

    this.segments = new ArrayList<>(segments);
  }

  public String toValue() {
    return segments.stream()
        .collect(Collectors.joining("-"));
  }

  @Override
  public int hashCode() {
    return Objects.hash(segments);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!(obj instanceof StreamId))
      return false;
    StreamId other = (StreamId) obj;
    return Objects.equals(segments, other.segments);
  }

  @Override
  public String toString() {
    return String.format("StreamId[value: %s]", toValue());
  }

  public static StreamId ofValue(String value) {
    String[] segmentsArray = value.split("-");

    return new StreamId(segmentsArray);
  }

  public static StreamId withRandom(String ...segments) {
    List<String> list = new LinkedList<>(Arrays.asList(segments));
    list.add(TSID.fast().toString());
    return new StreamId(list);
  }

  public static StreamId ofSegments(String ...segments) {
    return new StreamId(segments);
  }

  public String prefix() {
    return segments.stream()
        .limit(segments.size()-1)
        .collect(Collectors.joining("-"));
  }

  public String lastSegment() {
    return segments.getLast();
  }

  public int segmentSize() {
    return segments.size();
  }

  public String segment(int index) {
    return segments.get(index);
  }

  @Override
  public Iterator<String> iterator() {
    return segments.iterator();
  }

}
```

Projection is usd for creating views or reports:

```java
public abstract class Projection<S, E> {
  public abstract S initialState();
  public abstract S update(S currentState, Event<E> event);

  public S fold(S initialState, List<Event<E>> events) {
    S currentState = initialState;
    for(Event<E> e: events)
      currentState = update(currentState, e);
    return currentState;
  }

  public S fold(List<Event<E>> events) {
    return fold(initialState(), events);
  }

  public static <S, E> Projection<S, E> create(Supplier<S> initFn, BiFunction<S, Event<E>, S> updateFn) {
    Projection<S, E> p = new Projection<>() {

      @Override
      public S initialState() {
        return initFn.get();
      }

      @Override
      public S update(S currentState, Event<E> event) {
        return updateFn.apply(currentState, event);
      }
    };
    return p;
  }
}
```

### package `hr.fer.event.store`

Have basic elements needed for storing events.

```java
public abstract class EventStore<D> {
  protected final EventMapper<D> eventMapper;

  public EventStore(EventMapper<D> mapper) {
    eventMapper = mapper;
  }

  /**
   * Append event to store.
   *
   * If event version is -1 then it should be replaced with current version of the streamId
   *
   * @param event the event that should be saved
   */
  public abstract void append(Event<D> event);

  public void append(StreamId id, D eventData, Map<String, String> metaData) {
    @SuppressWarnings("unchecked")
    Class<? extends D> eventClass = (Class<? extends D>) eventData.getClass();
    TypeVersion vt = eventMapper.getEventTypeVersion(eventClass);
    append(Event.of(id, vt.type(), vt.version(), eventData, metaData));
  }

  public void appendAll(List<Event<D>> newEvents) {
    for(Event<D> event: newEvents) {
      append(event);
    }
  }

  public void append(StreamId id, D eventData) {
    append(id, eventData, Map.of());
  }

  public abstract List<Event<D>> getAllEvents();
  public abstract List<Event<D>> getAllEvents(StreamId streamId);

  /**
   * Returns all events from version.
   *
   * @param streamId
   * @param fromVersion
   * @return
   */
  public abstract List<Event<D>> getAllEventsFromVersion(StreamId streamId, int fromVersion);
  /**
   * Returns all events whose streamId has prefix.
   *
   * @param streamIdPrefix
   * @return
   */
  public abstract List<Event<D>> getAllEventsStreamIdPrefixIs(String streamIdPrefix);

  /**
   * Returns all events whose streamId has prefix that starts with.
   * @param streamIdPrefixStartsWith
   * @return
   */
  public abstract List<Event<D>> getAllEventsStreamIdPrefixStartsWith(String streamIdPrefixStartsWith);

  /**
   * Returns all events with event type
   *
   * @param eventDataClasses
   * @return
   */
  public abstract List<Event<D>> getAllEventsForEventDataClass(Class<? extends D> ...eventDataClasses);

}
```

As the objects that represent the data are saved in JSON, then it is necessary to return them back to the objects, so it is necessary to have the event type and version, because during the evolution of the event, we can have the same event recorded in different classes. For mapping from event type and version to class and vice versa we have `EventMapper`:

```java
public class EventMapper<D> {
  private static final ObjectMapper mapper = new ObjectMapper();

  public static record TypeVersion(String type, int version) {}

  public static record ClassTriple(String eventType, int eventTypeVersion, Class<?> clazz) {
    public TypeVersion typeVersion() {
      return new TypeVersion(eventType, eventTypeVersion);
    }
  }

  private Map<TypeVersion, Class<? extends D>> eventTypeVersonToClassMap;
  private Map<Class<? extends D>, TypeVersion> classToEventTypeVersionMap;

  @SuppressWarnings("unchecked")
  public EventMapper(List<ClassTriple> typeList) {
    this.classToEventTypeVersionMap = new HashMap<>();
    this.eventTypeVersonToClassMap = new HashMap<>();
    for(var ct: typeList) {
      classToEventTypeVersionMap.put((Class<? extends D>) ct.clazz(), ct.typeVersion());
      eventTypeVersonToClassMap.put(ct.typeVersion(), (Class<? extends D>) ct.clazz());
    }
  }

  public TypeVersion getEventTypeVersion(Class<? extends D> eventClass) {
    return classToEventTypeVersionMap.get(eventClass);
  }

  public String toJson(D eventData) {
    try {
      return mapper.writeValueAsString(eventData);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Can not convert event data object to JSON", e);
    }
  }

  public D toEventData(String jsonData, TypeVersion typeVersion) {
    TypeVersion eventTypeKey = typeVersion;
    try {
      D data = mapper.readValue(jsonData,
          eventTypeVersonToClassMap.get(eventTypeKey));
      return data;
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Can not parse event data (JSON) from DB", e);
    }
  }

  public static ClassTriple classTriple(String eventType, int eventTypeVersion, Class<?> clazz) {
    return new ClassTriple(eventType, eventTypeVersion, clazz);
  }
}
```

In this package there is concrete implementation of event store in memory:

```java
public class EventStoreInMemory<E> extends EventStore<E> {

  public EventStoreInMemory(EventMapper<E> mapper) {
    super(mapper);
  }

  private List<Event<E>> events = new LinkedList<>();

  @Override
  public void append(Event<E> event) {
    if(event.version() < 1)
      events.add(event.copyWithVersion(calculateNextVersion(event.streamId())));
    else
      events.add(event);
  }

  @Override
  public List<Event<E>> getAllEvents() {
    return Collections.unmodifiableList(events);
  }

  @Override
  public List<Event<E>> getAllEvents(StreamId streamId) {
    return events.stream()
      .filter(e -> e.streamId().equals(streamId))
      .sorted((e1, e2) -> Integer.compare(e1.version(), e2.version()))
      .collect(Collectors.toUnmodifiableList());
  }

  @Override
  public List<Event<E>> getAllEventsFromVersion(StreamId streamId, int fromVersion) {
    return events.stream()
        .filter(e -> e.streamId().equals(streamId) && e.version() >= fromVersion)
        .sorted((e1, e2) -> Integer.compare(e1.version(), e2.version()))
        .collect(Collectors.toUnmodifiableList());
  }

  @Override
  public List<Event<E>> getAllEventsStreamIdPrefixIs(String streamIdPrefix) {
    return events.stream()
        .filter(e -> e.streamId().prefix().equals(streamIdPrefix))
        .collect(Collectors.toUnmodifiableList());
  }

  @Override
  public List<Event<E>> getAllEventsStreamIdPrefixStartsWith(String streamIdPrefixStartsWith) {
    return events.stream()
        .filter(e -> e.streamId().prefix().startsWith(streamIdPrefixStartsWith))
        .collect(Collectors.toUnmodifiableList());
  }

  @Override
  public List<Event<E>> getAllEventsForEventDataClass(Class<? extends E> ...eventDataClasses) {
    Set<String> names = new HashSet<>();
    for(Class<? extends E> c: eventDataClasses) {
      names.add(eventMapper.getEventTypeVersion(c).type());
    }

    return events.stream()
        .filter(e -> names.contains(e.eventType()))
        .collect(Collectors.toUnmodifiableList());
  }

  private int calculateNextVersion(StreamId id) {
    return getAllEvents(id).size() + 1;
  }
}
```

### package `hr.fer.event.ddd`

In this package we have an abstract aggregate class that can be used in a concrete use case. The aggregate serves only to write (*write model*) and check the execution of commands. Object is constructed from a list of events. The `evolveState` method receives an event and changes the state of the aggregate. The `handle` method processes commands and returns a `Result`. Processing commands requires checking the previous state and accepting or rejecting commands. The `Result` object contains the processing result.

```java
public abstract class EventSourcesAggregate<D, C> {
  public EventSourcesAggregate(List<Event<D>> events) {
    validateEvents(events);
    evolveState(events);
  }

  protected void validateEvents(List<Event<D>> events) {
    if(events.isEmpty())
      throw new IllegalStateException("Aggregate not created.");
  }

  protected void evolveState(List<Event<D>> events) {
    for(var event: events) {
      evolveState(event);
    }
  }

  protected abstract void evolveState(Event<D> event);

  public abstract Result<? extends RuntimeException, Event<D>> handle(C command);

}
```
The result of event processing (`Result` object) throws an exception if the command is rejected. Regardless of whether the command is accepted or rejected, its processing can generate events that are in the result. These events need to be saved in the *store*.

```java
public class Result<E extends Exception, T> {
  private final E exception;
  private final List<T> events;

  public Result(E exception, List<T> events) {
    this.exception = exception;
    this.events = events;
  }

  public  static <A extends Exception, B> Result<A, B> exception(A exception) {
    return new Result<>(exception, List.of());
  }

  public  static <A extends Exception, B> Result<A, B> exception(A exception, List<B> events) {
    Assert.notNull(events, "Result events can not be null.");
    return new Result<>(exception, events);
  }

  public  static <A extends Exception, B> Result<A, B> exception(A exception, B ...events) {
    Assert.notEmpty(events, "Result events can not be null.");
    return new Result<>(exception, Arrays.asList(events));
  }

  public  static <A extends Exception, B> Result<A, B> events(B ...events) {
    Assert.notEmpty(events, "Events can not be empty.");
    return new Result<>(null, Arrays.asList(events));
  }

  public  static <A extends Exception, B> Result<A, B> events(List<B> events) {
    Assert.notEmpty(events, "Events can not be empty.");
    return new Result<>(null, events);
  }

  public E exception() {
    return exception;
  }

  public boolean isError() {
    return exception != null;
  }

  public List<T> events() {
    return events;
  }

  public boolean hasEvents() {
    return !events.isEmpty();
  }
}
```

### package `hr.fer.event.store.jpa`

It uses Spring Boot JPA to save events. This package includes the implementation of saving to the database. The whole project is made as a Spring Boot starter.

## Using

In `build.gradle` we need to pu dependencies:

```groovy
dependencies {
	implementation "hr.fer.tel.eventstore:spring-boot-starter-event-store-jpa:${version}"" // starter
	implementation 'org.springframework.boot:spring-boot-starter-data-jpa' // jpa
	implementation 'org.springframework.boot:spring-boot-starter-web'
	implementation 'io.hypersistence:hypersistence-utils-hibernate-63:3.7.5' // potrebno za TSID
	
	runtimeOnly 'org.postgresql:postgresql' // driver for DB

	testImplementation 'org.springframework.boot:spring-boot-starter-test'
	testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}
```

In thei example we have following packages:

- `hr.fer.icecream_truck` - basic functionality
- `hr.fer.icecream_truck.commands` - commands
- `hr.fer.icecream_truck.events` - event data

### Commands

```java
public sealed interface TruckCommand permits RestockFlavour, SellFlavour {
  StreamId truckId();
}

public record SellFlavour(StreamId truckId, FlavourName flavour, Map<String, String> metaData) implements TruckCommand {
}

public record RestockFlavour(StreamId truckId, FlavourName flavour, Amount amount, Map<String, String> metaData) implements TruckCommand {
}
```

### Events

```java
public sealed interface TruckEventData permits TruckCreatedEvent, StockChangeEvent, FlavourWentOutOfStock, FlavourWasNotInStock {
}

public record TruckCreatedEvent(String streamId) implements TruckEventData {
}

public sealed interface StockChangeEvent extends TruckEventData permits FlavourRestocked, FlavourSold {
}

public record FlavourRestocked(FlavourName flavour, Amount amount) implements StockChangeEvent {
}

public record FlavourSold(FlavourName flavour) implements StockChangeEvent {
}

public record FlavourWasNotInStock(FlavourName flavour) implements TruckEventData {
}

public record FlavourWentOutOfStock(FlavourName flavour) implements TruckEventData {
}
```

We see that the events do not have a reference to the stream. This is because these are not actually events but the data that is in the `Event` class, which has a reference to the stream.

### Value objects

```java
public record Amount(int value) {
  public Amount {
    if(value < 0)
      throw new IllegalArgumentException("Amount can not be negative.");
  }

  public Amount plus(Amount amount) {
    return new Amount(value + amount.value);
  }

  public Amount decrease() {
    return new Amount(value - 1);
  }
}


public record FlavourName(String name) {
}
```

### Creating events

To make it easier to create events, `TruckEventFactory` was created:

```java
package hr.fer.icecream_truck;

import java.util.List;
import java.util.Map;

import hr.fer.event.Event;
import hr.fer.event.StreamId;
import hr.fer.event.store.EventMapper;
import hr.fer.event.store.EventMapper.TypeVersion;
import hr.fer.icecream_truck.events.FlavourRestocked;
import hr.fer.icecream_truck.events.FlavourSold;
import hr.fer.icecream_truck.events.FlavourWasNotInStock;
import hr.fer.icecream_truck.events.FlavourWentOutOfStock;
import hr.fer.icecream_truck.events.TruckCreatedEvent;
import hr.fer.icecream_truck.events.TruckEventData;

public class TruckEventFactory {
  private EventMapper<TruckEventData> mapper;

  public TruckEventFactory() {
    this.mapper = new EventMapper<>(List.of(
      EventMapper.classTriple("truckCreated", 1, TruckCreatedEvent.class),
      EventMapper.classTriple("flavourWasNotInStock", 1, FlavourWasNotInStock.class),
      EventMapper.classTriple("flavorSold", 1, FlavourSold.class),
      EventMapper.classTriple("flavourWentOutOfStock", 1, FlavourWentOutOfStock.class),
      EventMapper.classTriple("flavourRestocked", 1, FlavourRestocked.class)
    ));
  }

  public EventMapper<TruckEventData> getMapper() {
    return mapper;
  }

  public Event<TruckEventData> flavourWasNotInStock(StreamId truckId, FlavourName flavour, Map<String, String> metaData) {
    FlavourWasNotInStock eventData = new FlavourWasNotInStock(flavour);
    TypeVersion et = mapper.getEventTypeVersion(FlavourWasNotInStock.class);
    return Event.of(truckId, et.type(), et.version(), eventData, metaData);
  }

  public Event<TruckEventData> flavourSold(StreamId truckId, FlavourName flavour, Map<String, String> metaData) {
    FlavourSold eventData = new FlavourSold(flavour);
    TypeVersion et = mapper.getEventTypeVersion(FlavourSold.class);
    return Event.of(truckId, et.type(), et.version(), eventData, metaData);
  }

  public Event<TruckEventData> flavourWentOutOfStock(StreamId truckId, FlavourName flavour, Map<String, String> metaData) {
    FlavourWentOutOfStock eventData = new FlavourWentOutOfStock(flavour);
    TypeVersion et = mapper.getEventTypeVersion(FlavourWentOutOfStock.class);
    return Event.of(truckId, et.type(), et.version(), eventData, metaData);
  }

  public Event<TruckEventData> createTruck(Map<String, String> metaData) {
    StreamId truckId = StreamId.ofPrefix("truck");
    TruckCreatedEvent eventData = new TruckCreatedEvent(truckId.toValue());
    TypeVersion et = mapper.getEventTypeVersion(TruckCreatedEvent.class);
    return Event.of(truckId, et.type(), et.version(), eventData, metaData);
  }

  public Event<TruckEventData> flavourRestocked(StreamId truckId, FlavourName flavourName, Amount amount, Map<String, String> metaData) {
    FlavourRestocked eventData = new FlavourRestocked(flavourName, amount);
    TypeVersion et = mapper.getEventTypeVersion(FlavourRestocked.class);
    return Event.of(truckId, et.type(), et.version(), eventData, metaData);
  }
}
```

It has an `EventMapper` for all event information and how it maps to event type and version.

In addition, we have one method factory for each event.

### `Truck` unit

The aggregate works independently of event saving. It is used to process commands and check whether the commands can be executed. It results in success/failure and a list of events to be saved.

```java
public class Truck extends EventSourcesAggregate<TruckEventData, TruckCommand> {

  private static record TruckState(StreamId truckId, HashMap<FlavourName, Amount> stock) {}
  private TruckEventFactory factory;

  private TruckState state;

  public Truck(List<Event<TruckEventData>> events) {
    super(events);

    factory = new TruckEventFactory();
  }



  @Override
  protected void validateEvents(List<Event<TruckEventData>> events) {
    super.validateEvents(events);

    if(!(events.get(0).eventData() instanceof TruckCreatedEvent))
      throw new IllegalStateException("First event should be creating truck.");
  }



  @Override
  protected void evolveState(Event<TruckEventData> event) {
    TruckEventData data = event.eventData();
    if(data instanceof TruckCreatedEvent tc) {
      state = new TruckState(StreamId.of(tc.streamId()), new HashMap<>());
    } else if(data instanceof StockChangeEvent sc) {
      switch (sc) {
      case FlavourRestocked(FlavourName flavour, Amount amount) ->
        state.stock().merge(flavour, amount, (oldValue, defaultValue) -> {
          return oldValue.plus(amount);
        });
      case FlavourSold(FlavourName flavour) ->
        state.stock().merge(flavour, new Amount(1), (oldValue, defaultValue) -> {
          return oldValue.decrease();
        });
      };
    }
  }

  public Amount getFlavourState(FlavourName flavour) {
    return getFlavourState().getOrDefault(flavour, new Amount(0));
  }

  public Map<FlavourName, Amount> getFlavourState() {
    HashMap<FlavourName, Amount> stock = state.stock();
    return Collections.unmodifiableMap(stock);
  }

  public StreamId getId() {
    return state.truckId();
  }

  public Result<? extends RuntimeException, Event<TruckEventData>> handle(TruckCommand command) {
    return switch(command) {
      case RestockFlavour restockCommand -> restock(restockCommand);
      case SellFlavour sellCommand -> sell(sellCommand);
    };
  }

  private Result<? extends RuntimeException, Event<TruckEventData>> sell(SellFlavour sellCommand) {
    StreamId truckId = state.truckId();
    FlavourName flavour = sellCommand.flavour();
    Map<String, String> metaData = sellCommand.metaData();

    Result<? extends RuntimeException, Event<TruckEventData>> result = switch(getFlavourState(flavour).value()) {
      case 0 -> Result.exception(
          new IllegalStateException("Can not sell. Stock is empty."),
          factory.flavourWasNotInStock(truckId, flavour, metaData));
      case 1 -> Result.events(
          factory.flavourSold(truckId, flavour, metaData),
          factory.flavourWentOutOfStock(truckId, flavour, metaData));
      default -> Result.events(factory.flavourSold(truckId, flavour, metaData));
    };

    if(result.hasEvents())
      evolveState(result.events());

    return result;
  }

  private Result<? extends RuntimeException, Event<TruckEventData>> restock(RestockFlavour restockCommand) {
    Event<TruckEventData> restockedEvent = factory.flavourRestocked(
        state.truckId(),
        restockCommand.flavour(),
        restockCommand.amount(),
        restockCommand.metaData());
    evolveState(restockedEvent);
    return Result.events(restockedEvent);
  }
}
```

### Service

The service facilitates the use of aggregates. It takes care of loading the aggregate and saving it, i.e. saving events.

```java
public class TruckService {
  TruckEventFactory factory;
  EventStore<TruckEventData> store;

  public TruckService(Function<EventMapper<TruckEventData>, EventStore<TruckEventData>> createNewStore) {
    factory = new TruckEventFactory();
    EventMapper<TruckEventData> mapper = factory.getMapper();
    store = createNewStore.apply(mapper);
  }

  public TruckService(EventRepository repo) {
    factory = new TruckEventFactory();
    store = new EventStoreDB<>(repo, factory.getMapper());
  }

  public StreamId createTruck(Map<String, String> metaData) {
    Event<TruckEventData> truckCreated = factory.createTruck(metaData);
    store.append(truckCreated);
    return truckCreated.streamId();
  }

  public void handle(TruckCommand command) {
    List<Event<TruckEventData>> events = store.getAllEvents(command.truckId());
    Truck truck = new Truck(events);

    Result<? extends RuntimeException, Event<TruckEventData>> result = truck.handle(command);
    store.appendAll(result.events());
    if(result.isError())
      throw result.exception();
  }

  // just for debugging
  public List<Event<TruckEventData>> getAllEvents(StreamId truckId) {
    return store.getAllEvents(truckId);
  }

  public <T> T getReport(StreamId truckId, Projection<T, TruckEventData> projection) {
     List<Event<TruckEventData>> events = store.getAllEvents(truckId);
     return projection.fold(events);
  }

  public <T> T getReport(Projection<T, TruckEventData> projection) {
    List<Event<TruckEventData>> events = store.getAllEventsStreamIdPrefixIs("truck");
    return projection.fold(events);
  }
}
```

### Reports and views

They represent *read models*. In fact, it is about implementations of projections. In our case, these projections are on demand, i.e. a method is called in the service that uses them to generate a view of the aggregate or aggregates.

Examples of projections:

`StockStateView` returns a map with the amount of ice cream of a particular flavor (`Map<FlavourName, Amount>`).

```java
public class StockStateView extends Projection<Map<FlavourName, Amount>, TruckEventData> {

  @Override
  public Map<FlavourName, Amount> initialState() {
    return Map.of();
  }

  @Override
  public Map<FlavourName, Amount> update(Map<FlavourName, Amount> currentState, Event<TruckEventData> event) {
    var newState = new HashMap<>(currentState);
    TruckEventData data = event.eventData();
    if(data instanceof StockChangeEvent sc)
      switch (sc) {
          case FlavourRestocked(FlavourName flavour, Amount amount) ->
            newState.merge(flavour, amount, (oldValue, defaultValue) -> {
              return oldValue.plus(amount);
            });
          case FlavourSold(FlavourName flavour) ->
            newState.merge(flavour, new Amount(1), (oldValue, defaultValue) -> {
              return oldValue.decrease();
            });
      };

    return newState;
  }
}
```

`SoldFlavourReport` calculates how many ice creams of a defined flavor have been sold.

```java
public class SoldFlavourReport extends Projection<Integer, TruckEventData> {

  private FlavourName flavour;

  public SoldFlavourReport(FlavourName flavour) {
    this.flavour = flavour;
  }

  @Override
  public Integer initialState() {
    return 0;
  }

  @Override
  public Integer update(Integer currentState, Event<TruckEventData> event) {
    return switch (event.eventData()) {
      case FlavourSold(FlavourName sold) -> sold.equals(flavour) ? currentState + 1 : currentState;
      default -> currentState;
    };
  }
}
```

`SoldFlavoursReport` calculates how many each ice cream flavors have been sold (`Map<FlavourName, Integer>`).

```java
public class SoldFlavoursReport extends Projection<Map<FlavourName, Integer>, TruckEventData> {

  @Override
  public Map<FlavourName, Integer> initialState() {
    return new HashMap<>();
  }

  @Override
  public Map<FlavourName, Integer> update(Map<FlavourName, Integer> currentState, Event<TruckEventData> event) {
    Map<FlavourName, Integer> newState = new HashMap<>(currentState);

    if(event.eventData() instanceof FlavourSold(FlavourName flavour))
          newState.merge(flavour, 1, (oldValue, defaultValue) -> {
            return oldValue + 1;
          });

    return newState;
  }
}
```

### Main

Usage example is in class `Main`:

```java
@Component
public class Main {
  private EventRepository repo;

  public Main(EventRepository repo) {
    this.repo = repo;
  }

  @PostConstruct
  void run() {
    basicExamples();
    evolveExample();
  }

  private void evolveExample() {
    System.out.println("==== evolve example");
    TruckService service = new TruckService(repo);
    Map<String, String> notImportantMetaData = Map.of();

    // creating truck
    StreamId truckId = service.createTruck(notImportantMetaData);
    System.out.println("Truck created with streamId: " + truckId);

    service.handle(new RestockFlavour(truckId, new FlavourName("vanilija"), new Amount(1), notImportantMetaData));
    System.out.println("Restocked vanilija: 1\n");

    printStateAndEvents(service.getAllEvents(truckId));

    service.handle(new SellFlavour(truckId, new FlavourName("vanilija"), notImportantMetaData));
    System.out.println("Sold vanailija: 1\n");

    printStateAndEvents(service.getAllEvents(truckId));
  }

  private void printStateAndEvents(List<Event<TruckEventData>> events) {
    System.out.println("Stock state: " + new StockStateView().fold(events) + "\n");
    System.out.println("Events: ");
    events.forEach(e -> System.out.println("\t" + e));
    System.out.println();
  }

  private void basicExamples() {
    System.out.println("==== basic example");

    TruckService service = new TruckService(repo);
    Map<String, String> notImportantMetaData = Map.of();



    StreamId truckId = service.createTruck(notImportantMetaData);
    System.out.println("Truck created with truckId: " + truckId);

    service.handle(new RestockFlavour(truckId, new FlavourName("vanilija"), new Amount(1), notImportantMetaData));
    System.out.println("Restocked vanilija: 1\n");

    service.handle(new SellFlavour(truckId, new FlavourName("vanilija"), notImportantMetaData));
    System.out.println("Sold vanailija: 1\n");


    try {
      System.out.println("Try to sell vanailija");
      service.handle(new SellFlavour(truckId, new FlavourName("vanilija"), notImportantMetaData));
    } catch (RuntimeException e) {
      System.out.println(e.getMessage() + "\n");
    }

    try {
      System.out.println("Try to sell jagoda");
      service.handle(new SellFlavour(truckId, new FlavourName("jagoda"), notImportantMetaData));
    } catch (RuntimeException e) {
      System.out.println(e.getMessage() + "\n");
    }

    System.out.println("Projection for solding vanilija: " +  service.getReport(truckId, new SoldFlavourReport(new FlavourName("vanilija"))) + "\n");
    System.out.println("Projection for all solds:\n" + service.getReport(truckId, new SoldFlavoursReport()) + "\n");

    System.out.println("Stock state:\n" + service.getReport(truckId, new StockStateView()) + "\n");

    System.out.println("Store report:");
    printStateAndEvents(service.getAllEvents(truckId));
  }
}
```

Printout is following:

```text
==== basic example
Truck created with truckId: StreamId[value: truck-0G7M8M0GPVDG1]
Restocked vanilija: 1

Sold vanailija: 1

Try to sell vanailija
Can not sell. Stock is empty.

Try to sell jagoda
Can not sell. Stock is empty.

Projection for solding vanilija: 1

Projection for all solds:
{FlavourName[name=vanilija]=1}

Stock state:
{FlavourName[name=vanilija]=Amount[value=0]}

Store report:
Stock state: {FlavourName[name=vanilija]=Amount[value=0]}

Events: 
	Event[streamId=StreamId[value: truck-0G7M8M0GPVDG1], version=1, eventType=truckCreated, eventTypeVersion=1, eventData=TruckCreatedEvent[streamId=truck-0G7M8M0GPVDG1], metaData={}]
	Event[streamId=StreamId[value: truck-0G7M8M0GPVDG1], version=2, eventType=flavourRestocked, eventTypeVersion=1, eventData=FlavourRestocked[flavour=FlavourName[name=vanilija], amount=Amount[value=1]], metaData={}]
	Event[streamId=StreamId[value: truck-0G7M8M0GPVDG1], version=3, eventType=flavorSold, eventTypeVersion=1, eventData=FlavourSold[flavour=FlavourName[name=vanilija]], metaData={}]
	Event[streamId=StreamId[value: truck-0G7M8M0GPVDG1], version=4, eventType=flavourWentOutOfStock, eventTypeVersion=1, eventData=FlavourWentOutOfStock[flavour=FlavourName[name=vanilija]], metaData={}]
	Event[streamId=StreamId[value: truck-0G7M8M0GPVDG1], version=5, eventType=flavourWasNotInStock, eventTypeVersion=1, eventData=FlavourWasNotInStock[flavour=FlavourName[name=vanilija]], metaData={}]
	Event[streamId=StreamId[value: truck-0G7M8M0GPVDG1], version=6, eventType=flavourWasNotInStock, eventTypeVersion=1, eventData=FlavourWasNotInStock[flavour=FlavourName[name=jagoda]], metaData={}]

==== evolve example
Truck created with streamId: StreamId[value: truck-0G7M8M1JJVDG2]
Restocked vanilija: 1

Stock state: {FlavourName[name=vanilija]=Amount[value=1]}

Events: 
	Event[streamId=StreamId[value: truck-0G7M8M1JJVDG2], version=1, eventType=truckCreated, eventTypeVersion=1, eventData=TruckCreatedEvent[streamId=truck-0G7M8M1JJVDG2], metaData={}]
	Event[streamId=StreamId[value: truck-0G7M8M1JJVDG2], version=2, eventType=flavourRestocked, eventTypeVersion=1, eventData=FlavourRestocked[flavour=FlavourName[name=vanilija], amount=Amount[value=1]], metaData={}]

Sold vanailija: 1

Stock state: {FlavourName[name=vanilija]=Amount[value=0]}

Events: 
	Event[streamId=StreamId[value: truck-0G7M8M1JJVDG2], version=1, eventType=truckCreated, eventTypeVersion=1, eventData=TruckCreatedEvent[streamId=truck-0G7M8M1JJVDG2], metaData={}]
	Event[streamId=StreamId[value: truck-0G7M8M1JJVDG2], version=2, eventType=flavourRestocked, eventTypeVersion=1, eventData=FlavourRestocked[flavour=FlavourName[name=vanilija], amount=Amount[value=1]], metaData={}]
	Event[streamId=StreamId[value: truck-0G7M8M1JJVDG2], version=3, eventType=flavorSold, eventTypeVersion=1, eventData=FlavourSold[flavour=FlavourName[name=vanilija]], metaData={}]
	Event[streamId=StreamId[value: truck-0G7M8M1JJVDG2], version=4, eventType=flavourWentOutOfStock, eventTypeVersion=1, eventData=FlavourWentOutOfStock[flavour=FlavourName[name=vanilija]], metaData={}]
```

### Tests

#### Projections

```java
class SoldFlavourReportTest {

  @Test
  void noEvents() {
    SoldFlavourReport report = new SoldFlavourReport(new FlavourName("a"));

    int result = report.fold(List.of());

    assertThat(result).isEqualTo(0);
  }

  @Test
  void oneSoldEvent() {
    TruckEventFactory factory = new TruckEventFactory();
    StreamId truckId = StreamId.ofPrefix("truck");

    SoldFlavourReport report = new SoldFlavourReport(new FlavourName("a"));

    int result = report.fold(List.of(
        factory.flavourSold(truckId, new FlavourName("a"), Map.of())
        ));

    assertThat(result).isEqualTo(1);
  }

  @Test
  void nSoldEvents() {
    TruckEventFactory factory = new TruckEventFactory();
    StreamId truckId = StreamId.ofPrefix("truck");

    SoldFlavourReport report = new SoldFlavourReport(new FlavourName("a"));

    int result = report.fold(List.of(
        factory.flavourSold(truckId, new FlavourName("a"), Map.of()),
        factory.flavourSold(truckId, new FlavourName("a"), Map.of()),
        factory.flavourSold(truckId, new FlavourName("a"), Map.of())
        ));

    assertThat(result).isEqualTo(3);
  }
}
```

#### Commands and aggregates

```java
package hr.fer.icecream_truck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import hr.fer.event.Event;
import hr.fer.event.StreamId;
import hr.fer.event.ddd.Result;
import hr.fer.icecream_truck.commands.RestockFlavour;
import hr.fer.icecream_truck.commands.SellFlavour;
import hr.fer.icecream_truck.events.TruckEventData;

class TruckTest {

  @Test
  void noEvents() {

    assertThrows(IllegalStateException.class, () -> {
      new Truck(List.of());
    });
  }

  @Test
  void initializeTruck() {
    TruckEventFactory factory = new TruckEventFactory();
    Map<String, String> notImportantMetaData = Map.of();
    Event<TruckEventData> truckCreated = factory.createTruck(notImportantMetaData);
    StreamId truckId = truckCreated.streamId();

    Truck truckAggregate = new Truck(List.of(
        truckCreated
        ));

    Map<FlavourName, Amount> state = truckAggregate.getFlavourState();
    assertThat(state).isEmpty();
    assertThat(truckAggregate.getId()).isEqualTo(truckId);
  }

  @Test
  void initializeTruckRestocked() {
    TruckEventFactory factory = new TruckEventFactory();
    Map<String, String> notImportantMetaData = Map.of();
    Event<TruckEventData> truckCreated = factory.createTruck(notImportantMetaData);
    StreamId truckId = truckCreated.streamId();

    Truck truckAggregate = new Truck(List.of(
        truckCreated,
        factory.flavourRestocked(truckId, new FlavourName("a"), new Amount(9), notImportantMetaData)
        ));

    Map<FlavourName, Amount> state = truckAggregate.getFlavourState();
    assertThat(state)
      .extractingFromEntries(e -> e.getKey().name(), e -> e.getValue().value())
      .containsOnly(
        tuple("a", 9)
        );
  }

  @Test
  void initializeTruckWithoutCreatingTruck() {
    TruckEventFactory factory = new TruckEventFactory();
    Map<String, String> notImportantMetaData = Map.of();
    StreamId truckId = StreamId.of("truck");

    RuntimeException ex = assertThrows(RuntimeException.class, () -> {
      new Truck(List.of(
          factory.flavourRestocked(truckId, new FlavourName("a"), new Amount(9), notImportantMetaData)
          ));
    });

    assertThat(ex).hasMessage("First event should be creating truck.");
  }

  @Test
  void initializeTruckRestockedTwice() {
    TruckEventFactory factory = new TruckEventFactory();
    Map<String, String> notImportantMetaData = Map.of();
    Event<TruckEventData> truckCreated = factory.createTruck(notImportantMetaData);
    StreamId truckId = truckCreated.streamId();

    Truck truckAggregate = new Truck(List.of(
        truckCreated,
        factory.flavourRestocked(truckId, new FlavourName("a"), new Amount(9), notImportantMetaData),
        factory.flavourRestocked(truckId, new FlavourName("a"), new Amount(3), notImportantMetaData)
        ));

    Map<FlavourName, Amount> state = truckAggregate.getFlavourState();
    assertThat(state)
      .extractingFromEntries(e -> e.getKey().name(), e -> e.getValue().value())
      .containsOnly(
        tuple("a", 12)
    );
  }

  @Test
  void initializeRestockedAndSold() {
    TruckEventFactory factory = new TruckEventFactory();
    Map<String, String> notImportantMetaData = Map.of();
    Event<TruckEventData> truckCreated = factory.createTruck(notImportantMetaData);
    StreamId truckId = truckCreated.streamId();

    Truck truckAggregate = new Truck(List.of(
        truckCreated,
        factory.flavourRestocked(truckId, new FlavourName("a"), new Amount(9), notImportantMetaData),
        factory.flavourSold(truckId, new FlavourName("a"), notImportantMetaData)
        ));

    Map<FlavourName, Amount> state = truckAggregate.getFlavourState();
    assertThat(state)
      .extractingFromEntries(e -> e.getKey().name(), e -> e.getValue().value())
      .containsOnly(
        tuple("a", 8)
        );
  }

  @Test
  void restockCommand() {
    TruckEventFactory factory = new TruckEventFactory();
    Map<String, String> notImportantMetaData = Map.of();
    Event<TruckEventData> truckCreated = factory.createTruck(notImportantMetaData);
    StreamId truckId = truckCreated.streamId();
    Truck truckAggregate = new Truck(List.of(
        truckCreated
        ));

    Result<? extends RuntimeException, Event<TruckEventData>> result = truckAggregate.handle(new RestockFlavour(truckId, new FlavourName("a"), new Amount(3), notImportantMetaData));

    assertThat(result.isError()).isFalse();
    assertThat(result.events())
      .extracting("eventType")
      .containsExactly("flavourRestocked");
    Map<FlavourName, Amount> state = truckAggregate.getFlavourState();
    assertThat(state)
      .extractingFromEntries(e -> e.getKey().name(), e -> e.getValue().value())
      .containsOnly(
        tuple("a", 3)
        );
  }

  @Test
  void sellCommand() {
    TruckEventFactory factory = new TruckEventFactory();
    Map<String, String> notImportantMetaData = Map.of();
    Event<TruckEventData> truckCreated = factory.createTruck(notImportantMetaData);
    StreamId truckId = truckCreated.streamId();
    Truck truckAggregate = new Truck(List.of(
        truckCreated,
        factory.flavourRestocked(truckId, new FlavourName("a"), new Amount(9), notImportantMetaData)
        ));

    var result = truckAggregate.handle(new SellFlavour(truckId, new FlavourName("a"), notImportantMetaData));

    assertThat(result.isError()).isFalse();
    assertThat(result.events())
      .extracting("eventType")
      .containsExactly("flavorSold");
    Map<FlavourName, Amount> state = truckAggregate.getFlavourState();
    assertThat(state)
      .extractingFromEntries(e -> e.getKey().name(), e -> e.getValue().value())
      .containsOnly(
        tuple("a", 8)
        );
  }

  @Test
  void sellCommand_whenNothingLeftInStock() {
    TruckEventFactory factory = new TruckEventFactory();
    Map<String, String> notImportantMetaData = Map.of();
    Event<TruckEventData> truckCreated = factory.createTruck(notImportantMetaData);
    StreamId truckId = truckCreated.streamId();
    Truck truckAggregate = new Truck(List.of(
        truckCreated,
        factory.flavourRestocked(truckId, new FlavourName("a"), new Amount(1), notImportantMetaData)
        ));

    var result = truckAggregate.handle(new SellFlavour(truckId, new FlavourName("a"), notImportantMetaData));

    assertThat(result.isError()).isFalse();
    assertThat(result.events())
    .extracting("eventType")
    .containsExactly("flavorSold", "flavourWentOutOfStock");
    Map<FlavourName, Amount> state = truckAggregate.getFlavourState();
    assertThat(state)
    .extractingFromEntries(e -> e.getKey().name(), e -> e.getValue().value())
    .containsOnly(
        tuple("a", 0)
        );
  }

  @Test
  void sellCommand_whenNothingPutInStock() {
    TruckEventFactory factory = new TruckEventFactory();
    Map<String, String> notImportantMetaData = Map.of();
    Event<TruckEventData> truckCreated = factory.createTruck(notImportantMetaData);
    StreamId truckId = truckCreated.streamId();
    Truck truckAggregate = new Truck(List.of(
        truckCreated
        ));

    var result = truckAggregate.handle(new SellFlavour(truckId, new FlavourName("a"), notImportantMetaData));

    assertThat(result.isError()).isTrue();
    assertThat(result.events())
    .extracting("eventType")
    .containsExactly("flavourWasNotInStock");
    Map<FlavourName, Amount> state = truckAggregate.getFlavourState();
    assertThat(state).isEmpty();
  }

  @Test
  void sellCommand_whenEmptyStock() {
    TruckEventFactory factory = new TruckEventFactory();
    Map<String, String> notImportantMetaData = Map.of();
    Event<TruckEventData> truckCreated = factory.createTruck(notImportantMetaData);
    StreamId truckId = truckCreated.streamId();
    Truck truckAggregate = new Truck(List.of(
        truckCreated,
        factory.flavourRestocked(truckId, new FlavourName("a"), new Amount(0), notImportantMetaData)
        ));

    var result = truckAggregate.handle(new SellFlavour(truckId, new FlavourName("a"), notImportantMetaData));

    assertThat(result.isError()).isTrue();
    assertThat(result.events())
      .extracting("eventType")
      .containsExactly("flavourWasNotInStock");
    Map<FlavourName, Amount> state = truckAggregate.getFlavourState();
    assertThat(state)
      .extractingFromEntries(e -> e.getKey().name(), e -> e.getValue().value())
      .containsOnly(
        tuple("a", 0)
      );
  }
}
```

#### Service

```java
class TruckServiceTest {
  static Projection<HashSet<StreamId>, TruckEventData> truckIdsProjection = Projection
    .create(() -> new HashSet<StreamId>(), (s, e) -> {
      if (e.eventType().equals("truckCreated")) {
        s.add(e.streamId());
      }
      return s;
    });

  @Test
  void truckNotCreated() throws Exception {
    TruckService service = new TruckService(EventStoreInMemory::new);

    Set<StreamId> truckIds = service.getReport(truckIdsProjection);

    assertThat(truckIds).hasSize(0);
  }

  @Test
  void oneTruckCreated() throws Exception {
    TruckService service = new TruckService(EventStoreInMemory::new);
    Map<String, String> notImportantMetaData = Map.of();

    StreamId createdTruckId = service.createTruck(notImportantMetaData);

    Set<StreamId> truckIds = service.getReport(truckIdsProjection);
    assertThat(truckIds).hasSize(1);
    assertThat(service.getAllEvents(createdTruckId))
      .extracting("eventType")
      .containsExactly("truckCreated");
  }

  @Test
  void restockFlavour() throws Exception {
    TruckService service = new TruckService(EventStoreInMemory::new);
    Map<String, String> notImportantMetaData = Map.of();
    StreamId truckId = service.createTruck(notImportantMetaData);

    service.handle(new RestockFlavour(truckId, new FlavourName("vanilija"), new Amount(10), notImportantMetaData));

    Map<FlavourName, Amount> stockReport = service.getReport(truckId, new StockStateView());

    assertThat(stockReport)
      .extractingFromEntries(e -> e.getKey().name(), e -> e.getValue().value())
      .containsOnly(
          tuple("vanilija", 10)
      );
    assertThat(service.getAllEvents(truckId))
      .extracting("eventType")
      .containsExactly("truckCreated", "flavourRestocked");
  }

  @Test
  void sellFlavour() throws Exception {
    TruckService service = new TruckService(EventStoreInMemory::new);
    Map<String, String> notImportantMetaData = Map.of();
    StreamId truckId = service.createTruck(notImportantMetaData);
    service.handle(new RestockFlavour(truckId, new FlavourName("vanilija"), new Amount(10), notImportantMetaData));

    service.handle(new SellFlavour(truckId, new FlavourName("vanilija"), notImportantMetaData));

    Map<FlavourName, Amount> stockReport = service.getReport(truckId, new StockStateView());

    assertThat(stockReport)
      .extractingFromEntries(e -> e.getKey().name(), e -> e.getValue().value())
      .containsOnly(
          tuple("vanilija", 9)
      );
    assertThat(service.getAllEvents(truckId))
      .extracting("eventType")
      .containsExactly("truckCreated", "flavourRestocked", "flavorSold");
  }

  @Test
  void sellFlavourWhenNotInStock() throws Exception {
    TruckService service = new TruckService(EventStoreInMemory::new);
    Map<String, String> notImportantMetaData = Map.of();
    StreamId truckId = service.createTruck(notImportantMetaData);
    service.handle(new RestockFlavour(truckId, new FlavourName("vanilija"), new Amount(0), notImportantMetaData));

    assertThrows(RuntimeException.class, () -> {
      service.handle(new SellFlavour(truckId, new FlavourName("vanilija"), notImportantMetaData));
    });

    Map<FlavourName, Amount> stockReport = service.getReport(truckId, new StockStateView());
    assertThat(stockReport)
      .extractingFromEntries(e -> e.getKey().name(), e -> e.getValue().value())
      .containsOnly(
          tuple("vanilija", 0)
          );
    assertThat(service.getAllEvents(truckId))
      .extracting("eventType")
      .containsExactly("truckCreated", "flavourRestocked", "flavourWasNotInStock");
  }

  @Test
  void sellFlavourWhenNotJetInStock() throws Exception {
    TruckService service = new TruckService(EventStoreInMemory::new);
    Map<String, String> notImportantMetaData = Map.of();
    StreamId truckId = service.createTruck(notImportantMetaData);

    assertThrows(RuntimeException.class, () -> {
      service.handle(new SellFlavour(truckId, new FlavourName("vanilija"), notImportantMetaData));
    });


    Map<FlavourName, Amount> stockReport = service.getReport(truckId, new StockStateView());
    assertThat(stockReport).isEmpty();
    assertThat(service.getAllEvents(truckId))
      .extracting("eventType")
      .containsExactly("truckCreated", "flavourWasNotInStock");
  }

  @Test
  void sellFlavourAndItWentOutOfStock() throws Exception {
    TruckService service = new TruckService(EventStoreInMemory::new);
    Map<String, String> notImportantMetaData = Map.of();
    StreamId truckId = service.createTruck(notImportantMetaData);
    service.handle(new RestockFlavour(truckId, new FlavourName("vanilija"), new Amount(1), notImportantMetaData));

    service.handle(new SellFlavour(truckId, new FlavourName("vanilija"), notImportantMetaData));

    Map<FlavourName, Amount> stockReport = service.getReport(truckId, new StockStateView());

    assertThat(stockReport)
      .extractingFromEntries(e -> e.getKey().name(), e -> e.getValue().value())
      .containsOnly(
          tuple("vanilija", 0)
      );
    assertThat(service.getAllEvents(truckId))
      .extracting("eventType")
      .containsExactly("truckCreated", "flavourRestocked", "flavorSold", "flavourWentOutOfStock");
  }
}
```
