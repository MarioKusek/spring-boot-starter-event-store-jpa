package hr.fer.eventstore.base;

import java.util.List;

public interface EventProducer<E> {
  List<E> produce(List<E> events);
}
