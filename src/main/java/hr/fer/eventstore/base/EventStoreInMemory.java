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
  public void append(Event<E> eventData) {
    events.add(eventData);
  }

  @Override
  public List<Event<E>> getAllEvents() {
    return Collections.unmodifiableList(events);
  }

  @Override
  public List<Event<E>> getAllEvents(String streamId) {
    return events.stream()
      .filter(e -> e.streamId().equals(streamId))
      .collect(Collectors.toUnmodifiableList());
  }

  @Override
  public void evolve(EventProducer<E> eventProducer) {
    appendAll(eventProducer.produce(getAllEvents()));
  }

}
