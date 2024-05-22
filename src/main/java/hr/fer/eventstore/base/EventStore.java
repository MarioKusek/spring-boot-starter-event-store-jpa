package hr.fer.eventstore.base;

import java.util.List;
import java.util.Map;

public interface EventStore<D> {
  // TODO remove - use with streamId
  void append(Event<D> eventData);
  void append(String streamId, D eventData, Map<String,String> metaData);
  void appendAll(List<Event<D>> events);

  default void append(String streamId, D eventData) {
    append(streamId, eventData, Map.of());
  }

  List<Event<D>> getAllEvents();
  List<Event<D>> getAllEvents(String streamId);

  void evolve(EventProducer<D> eventProducer);
}