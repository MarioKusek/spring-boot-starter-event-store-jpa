package hr.fer.event.store.jpa;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import hr.fer.event.StreamId;

public interface EventRepository {
  Collection<EventJpaEntity> findAll();
  Optional<EventJpaEntity> findById(long id);
  List<EventJpaEntity> findAllByStreamId(StreamId streamId);
  Optional<EventJpaEntity> findByStreamIdAndVersion(StreamId streamId, int version);
  List<EventJpaEntity> findAllByStreamIdAndFromVersion(StreamId streamId, int fromVersion);
  List<EventJpaEntity> getAllEventsStreamIdPrefixIs(String streamIdPrefix);
  List<EventJpaEntity> getAllEventsStreamIdPrefixStartsWith(String streamIdPrefixStartsWith);
  List<EventJpaEntity> getAllEventsForEventDataClass(Set<String> eventTypeNames);

  long countByStreamId(String streamId);

  void save(EventJpaEntity eventEntity);
  void saveAll(List<EventJpaEntity>  list);

  void deleteAll();

}
