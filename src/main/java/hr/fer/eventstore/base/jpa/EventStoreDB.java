package hr.fer.eventstore.base.jpa;

import java.util.List;

import hr.fer.eventstore.base.Event;
import hr.fer.eventstore.base.EventMapper;
import hr.fer.eventstore.base.EventStore;
import io.hypersistence.tsid.TSID.Factory;
import jakarta.transaction.Transactional;

public class EventStoreDB<D> extends EventStore<D> {
  private EventMapper<D> eventMapper;
  private EventJpaRepository repo;

  public EventStoreDB(EventJpaRepository repo, EventMapper<D> mapper) {
    super(mapper);
    this.repo = repo;
    eventMapper = mapper;
  }

  @Override
  @Transactional
  public void append(Event<D> event) {
    EventJpaEntity eventEntity = createEventEntity(event);
    repo.save(eventEntity);
  }

  @Override
  @Transactional
  public void appendAll(List<Event<D>> newEvents) {
    super.appendAll(newEvents);
  }

  @Override
  public List<Event<D>> getAllEvents() {
  return repo.findAll().stream()
      .map(eventMapper::toEvent)
      .toList();
  }

  @Override
  public List<Event<D>> getAllEvents(String streamId) {
    return repo.findAllByStreamId(streamId).stream()
        .map(eventMapper::toEvent)
        .toList();
  }

    private EventJpaEntity createEventEntity(Event<D> event) {
    EventJpaEntity eventEntity = createEventEntity(event, calculateNextVersion(event.streamId()));
    return eventEntity;
  }

  private EventJpaEntity createEventEntity(Event<D> event, int version) {
    EventJpaEntity eventEntity = new EventJpaEntity(
        Factory.getTsid().toLong(),
        event.streamId(),
        version,
        event.eventType(),
        event.eventTypeVersion(),
        eventMapper.toJson(event.eventData()),
        event.metaData()
        );
    return eventEntity;
  }

  private int calculateNextVersion(String streamId) {
    return (int) repo.countByStreamId(streamId);
  }

}
