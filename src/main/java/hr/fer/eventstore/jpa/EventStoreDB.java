package hr.fer.eventstore.jpa;

import java.util.List;

import hr.fer.eventstore.base.Event;
import hr.fer.eventstore.base.EventMapper;
import hr.fer.eventstore.base.EventMapper.TypeVersion;
import hr.fer.eventstore.base.EventStore;
import hr.fer.eventstore.base.StreamId;
import io.hypersistence.tsid.TSID.Factory;
import jakarta.transaction.Transactional;

public class EventStoreDB<D> extends EventStore<D> {
  private EventMapper<D> eventMapper;
  private EventRepository repo;

  public EventStoreDB(EventRepository repo, EventMapper<D> mapper) {
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
      .map(EventStoreDB.this::toEvent)
      .toList();
  }

  @Override
  public List<Event<D>> getAllEvents(StreamId streamId) {
    return repo.findAllByStreamId(streamId).stream()
      .map(EventStoreDB.this::toEvent)
      .toList();
  }

  private Event<D> toEvent(EventJpaEntity eventEntity) {
    D data = eventMapper.toEventData(eventEntity.getData(),
        new TypeVersion(eventEntity.getEventType(), eventEntity.getEventTypeVersion()));

    return new Event<>(
        StreamId.of(eventEntity.getStreamId()),
        eventEntity.getEventType(),
        eventEntity.getEventTypeVersion(),
        data,
        eventEntity.getMeta());
  }


  private EventJpaEntity createEventEntity(Event<D> event) {
    EventJpaEntity eventEntity = createEventEntity(event, calculateNextVersion(event.streamId()));
    return eventEntity;
  }

  private EventJpaEntity createEventEntity(Event<D> event, int version) {
    EventJpaEntity eventEntity = new EventJpaEntity(
        Factory.getTsid().toLong(),
        event.streamId().toValue(),
        version,
        event.eventType(),
        event.eventTypeVersion(),
        eventMapper.toJson(event.eventData()),
        event.metaData());
    return eventEntity;
  }

  private int calculateNextVersion(StreamId id) {
    return (int) repo.countByStreamId(id.toValue());
  }

}