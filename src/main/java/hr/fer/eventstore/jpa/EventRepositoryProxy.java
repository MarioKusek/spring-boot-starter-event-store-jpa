package hr.fer.eventstore.jpa;

import java.util.Collection;
import java.util.List;

import jakarta.persistence.EntityManager;

public class EventRepositoryProxy implements EventRepository {
  private EventJpaRepository repo;
  private EntityManager em;

  public EventRepositoryProxy(EntityManager em, EventJpaRepository repo) {
    this.em = em;
    this.repo = repo;
  }

  @Override
  public long countByStreamId(String streamId) {
    return repo.countByStreamId(streamId);
  }

  @Override
  public List<EventJpaEntity> findAllByStreamId(String streamId) {
    return repo.findAllByStreamId(streamId);
  }

  @Override
  public void save(EventJpaEntity eventEntity) {
    repo.save(eventEntity);
  }

  @Override
  public Collection<EventJpaEntity> findAll() {
    return repo.findAll();
  }

  @Override
  public void deleteAll() {
    repo.deleteAll();
  }

}
