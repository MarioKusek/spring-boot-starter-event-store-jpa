package hr.fer.eventstore.jpa;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

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

  @Transactional(readOnly = true)
  @Override
  public List<EventJpaEntity> findAllByStreamId(String streamId) {
    return entityManager.createQuery("""
        select e
        from EventJpaEntity e
        where
            e.streamId = ?1
        order by
            e.id asc
        """)
      .setParameter(1, streamId)
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
