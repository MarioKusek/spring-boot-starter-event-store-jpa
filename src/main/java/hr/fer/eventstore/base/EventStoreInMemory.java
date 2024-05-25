package hr.fer.eventstore.base;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EventStoreInMemory<E> extends EventStore<E> {

  public EventStoreInMemory(EventMapper<E> mapper) {
    super(mapper);
  }

  private List<Event<E>> events = new LinkedList<>();

  @Override
  public void append(Event<E> event) {
    events.add(event);
  }

  @Override
  public void appendAll(List<Event<E>> newEvents) {
    events.addAll(newEvents);
  }

  @Override
  public List<Event<E>> getAllEvents() {
    return Collections.unmodifiableList(events);
  }

  @Override
  public void evolve(EventProducer<E> eventProducer) {
    appendAll(eventProducer.produce(getAllEvents()));
  }

  @Override
  public void append(String streamId, E eventData, Map<String, String> metaData) {
    // TODO implementirati
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Event<E>> getAllEvents(String streamId) {
    // TODO implementirati
    throw new UnsupportedOperationException();
  }

  @Override
  public void evolve(String streamId, EventProducer<E> eventProducer) {
    // TODO implementirati
    throw new UnsupportedOperationException();
  }

}
