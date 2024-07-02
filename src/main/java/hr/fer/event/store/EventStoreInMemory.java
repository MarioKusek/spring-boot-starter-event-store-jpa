package hr.fer.event.store;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import hr.fer.event.Event;
import hr.fer.event.StreamId;

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
  public Optional<Event<E>> getEvent(StreamId streamId, int version) {
    return events.stream()
        .filter(e -> e.streamId().equals(streamId) && e.version() == version)
        .findFirst();
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
