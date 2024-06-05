package hr.fer.event.store.jpa;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import hr.fer.event.StreamId;
import jakarta.persistence.EntityManager;

@Component
public class EventRepositoryWithEntityManager implements EventRepository {
  private EntityManager entityManager;

  public EventRepositoryWithEntityManager(EntityManager em) {
    this.entityManager = em;
  }

  @Transactional(readOnly = true)
  @Override
  public long countByStreamId(String streamId) {
    return (Long) entityManager.createQuery("""
        select count(*)
        from EventJpaEntity e
        where
            e.streamId = ?1
        """)
      .setParameter(1, streamId)
      .getSingleResult();
  }

  @Transactional(readOnly = true)
  @Override
  public List<EventJpaEntity> findAllByStreamId(StreamId streamId) {
    return entityManager.createQuery("""
        select e
        from EventJpaEntity e
        where
            e.streamId = ?1
        order by
            e.id asc
        """)
      .setParameter(1, streamId.toValue())
      .getResultList();
  }

  @Transactional(readOnly = true)
  @Override
  public List<EventJpaEntity> findAllByStreamIdAndFromVersion(StreamId streamId, int fromVersion) {
    return entityManager.createQuery("""
        select e
        from EventJpaEntity e
        where
            e.streamId = ?1 and
            e.version >= ?2
        order by
            e.id asc
        """)
      .setParameter(1, streamId.toValue())
      .setParameter(2, fromVersion)
      .getResultList();
  }

  @Transactional(readOnly = true)
  @Override
  public List<EventJpaEntity> getAllEventsStreamIdPrefixIs(String streamIdPrefix) {
    return entityManager.createQuery("""
        select e
        from EventJpaEntity e
        where
            e.streamId like concat( ?1, '-%')
        order by
            e.id asc
        """)
      .setParameter(1, streamIdPrefix)
      .getResultList();
  }

  @Transactional(readOnly = true)
  @Override
  public List<EventJpaEntity> getAllEventsStreamIdPrefixStartsWith(String streamIdPrefixStartsWith) {
    return entityManager.createQuery("""
        select e
        from EventJpaEntity e
        where
            e.streamId like concat( ?1, '%')
        order by
            e.id asc
        """)
      .setParameter(1, streamIdPrefixStartsWith)
      .getResultList();
  }

  @Transactional(readOnly = true)
  @Override
  public List<EventJpaEntity> getAllEventsForEventDataClass(Set<String> eventTypeNames) {
    String wherePart = eventTypeNames.stream()
      .map(name -> "e.eventType = '" + name + "'")
      .collect(Collectors.joining(" or "));

    String queryString = """
        select e
        from EventJpaEntity e
        where
        """ +
        wherePart;

    return entityManager
            .createQuery(queryString)
            .getResultList();
  }



  @Transactional
  @Override
  public void save(EventJpaEntity eventEntity) {
    entityManager.persist(eventEntity);
  }

  @Transactional(readOnly = true)
  @Override
  public Collection<EventJpaEntity> findAll() {
    return entityManager.createQuery("""
        select e
        from EventJpaEntity e
        order by
            e.id asc
        """)
      .getResultList();
  }

  @Transactional
  @Override
  public void deleteAll() {
    entityManager.createQuery("""
            delete
            from EventJpaEntity e
            """).executeUpdate();
  }

  @Override
  public Optional<EventJpaEntity> findById(long id) {
    return entityManager.createQuery("""
        select e
        from EventJpaEntity e
        where
            e.id = ?1
        """)
      .setParameter(1, id)
      .getResultStream()
      .findFirst();
  }

  @Transactional
  @Override
  public void saveAll(List<EventJpaEntity> list) {
    for(var e: list)
      save(e);
  }

}
