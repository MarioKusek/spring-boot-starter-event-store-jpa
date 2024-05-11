package hr.fer.eventstore.base;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class EventStoreInMemory<E> implements EventStore<E> {
  private List<Event<E>> events = new LinkedList<>();

  @Override
  public void append(Event<E> event) {
    events.add(event);
  }

  @Override
  public void append(List<Event<E>> newEvents) {
    events.addAll(newEvents);
  }

  @Override
  public List<Event<E>> getAllEvents() {
    return Collections.unmodifiableList(events);
  }

  @Override
  public void evolve(EventProducer<Event<E>> eventProducer) {
    append(eventProducer.produce(getAllEvents()));
  }

}
