package hr.fer.eventstore.jpa;

import java.util.Collection;
import java.util.List;

public interface EventRepository {
  long countByStreamId(String streamId);
  List<EventJpaEntity> findAllByStreamId(String streamId);
  void save(EventJpaEntity eventEntity);
  Collection<EventJpaEntity> findAll();
  void deleteAll();

}
