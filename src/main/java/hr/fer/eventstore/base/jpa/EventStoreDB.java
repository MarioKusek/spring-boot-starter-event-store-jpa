package hr.fer.eventstore.base.jpa;

import java.util.List;
import java.util.Map;

import hr.fer.eventstore.base.Event;
import hr.fer.eventstore.base.EventMapper;
import hr.fer.eventstore.base.EventProducer;
import hr.fer.eventstore.base.EventStore;
import hr.fer.eventstore.base.EventMapper.TypeVersion;
import io.hypersistence.tsid.TSID.Factory;
import jakarta.transaction.Transactional;

public class EventStoreDB<D> implements EventStore<D> {
  private EventMapper<D> eventMapper;
  private EventJpaRepository repo;

  public EventStoreDB(EventJpaRepository repo, EventMapper<D> mapper) {
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
  public void append(String streamId, D eventData, Map<String, String> metaData) {
    Class<? extends D> eventClass = (Class<? extends D>) eventData.getClass();
    TypeVersion vt = eventMapper.getEventTypeVersion(eventClass);
    append(new Event<>(streamId, vt.type(), vt.version(), eventData, metaData));
  }

  @Override
  @Transactional
  public void appendAll(List<Event<D>> newEvents) {
    for(Event<D> event: newEvents) {
      int nextVersion = calculateNextVersion(event.streamId());
      EventJpaEntity eventEntity = createEventEntity(event, nextVersion);
      repo.save(eventEntity);
    }
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

  @Override
  public void evolve(EventProducer<D> eventProducer) {
    List<Event<D>> allEvents = getAllEvents();
    List<Event<D>> produced = eventProducer.produce(allEvents);
    appendAll(produced);
  }

  @Override
  public void evolve(String streamId, EventProducer<D> eventProducer) {
    List<Event<D>> allStreamEvents = getAllEvents(streamId);
    List<Event<D>> produced = eventProducer.produce(allStreamEvents);
    appendAll(produced);
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
