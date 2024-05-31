package hr.fer.eventstore.jpa;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import hr.fer.eventstore.base.StreamId;

public interface EventRepository {
  Collection<EventJpaEntity> findAll();
  Optional<EventJpaEntity> findById(long id);
  List<EventJpaEntity> findAllByStreamId(StreamId streamId);
  List<EventJpaEntity> findAllByStreamIdAndFromVersion(StreamId streamId, int fromVersion);
  // dodati 3 query-a:
  // TODO sve agregate čiji streamID počinje s nekim tekstom
  // TODO za sve agregate i pojedine tipove događaja

  long countByStreamId(String streamId);

  void save(EventJpaEntity eventEntity);
  void saveAll(List<EventJpaEntity>  list);

  void deleteAll();

}
