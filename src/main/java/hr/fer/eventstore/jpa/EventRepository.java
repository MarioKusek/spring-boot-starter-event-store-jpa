package hr.fer.eventstore.jpa;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EventRepository {
  long countByStreamId(String streamId);
  List<EventJpaEntity> findAllByStreamId(String streamId);
  void save(EventJpaEntity eventEntity);
  Collection<EventJpaEntity> findAll();
  void deleteAll();
  Optional<EventJpaEntity> findById(long id);
  void saveAll(List<EventJpaEntity>  list);

}
