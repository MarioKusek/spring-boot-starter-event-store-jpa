package hr.fer.event;

import java.util.List;

public interface EventProducer<E> {
  List<Event<E>> produce(List<Event<E>> events);
}
