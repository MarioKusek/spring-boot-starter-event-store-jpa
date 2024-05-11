package hr.fer.eventstore.base;

import java.util.List;

public interface EventStore<D> {
  void append(Event<D> event);
  void append(List<Event<D>> newEventsData);
  List<Event<D>> getAllEvents();
  void evolve(EventProducer<Event<D>> eventProducer);
}