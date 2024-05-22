package hr.fer.eventstore.base;

import java.util.List;

public interface EventProducer<E> {
  List<Event<E>> produce(List<Event<E>> events);
}
