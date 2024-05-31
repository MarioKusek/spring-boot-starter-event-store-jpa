package hr.fer.eventstore.base;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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
    // TODO implement
    throw new UnsupportedOperationException();
  }

  private int calculateNextVersion(StreamId id) {
    return getAllEvents(id).size() + 1;
  }

}
