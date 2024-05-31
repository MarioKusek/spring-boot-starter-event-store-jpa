package hr.fer.eventstore.jpa;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import hr.fer.eventstore.base.StreamId;
import jakarta.persistence.EntityManager;

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

  // TODO dodati 3 query-a:
  // @Query("SELECT u.username FROM User u WHERE u.username LIKE CONCAT('%',:username,'%')") - sve agregate
  // za sve agregate i pojedine tipove događaja
  // stream id i varzija od koje želimo događaje
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
