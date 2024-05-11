package hr.fer.eventstore.base;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import hr.fer.eventstore.base.jpa.EventJpaEntity;
import hr.fer.eventstore.base.jpa.EventJpaRepository;
import io.hypersistence.tsid.TSID.Factory;
import jakarta.transaction.Transactional;

public class EventStoreDB<D> implements EventStore<D> {
  private Map<String, Class<? extends D>> eventTypeToClassMap;
  private static ObjectMapper mapper = new ObjectMapper();

  private EventJpaRepository repo;

  public EventStoreDB(EventJpaRepository repo, Map<String, Class<? extends D>> eventTypeToClassMap) {
    this.repo = repo;
    this.eventTypeToClassMap = eventTypeToClassMap;
  }

  @Override
  @Transactional
  public void append(Event<D> event) {
    EventJpaEntity eventEntity = createEventEntity(event);
    repo.save(eventEntity);
  }

  @Override
  @Transactional
  public void append(List<Event<D>> newEvents) {
    for(Event<D> event: newEvents) {
      int nextVersion = calculateNextVersion(event.streamId());
      EventJpaEntity eventEntity = createEventEntity(event, nextVersion);
      repo.save(eventEntity);
    }
  }

  @Override
  public List<Event<D>> getAllEvents() {
  return repo.findAll().stream()
      .map(this::toEvent)
      .toList();
  }

  @Override
  public void evolve(EventProducer<Event<D>> eventProducer) {
    append(eventProducer.produce(getAllEvents()));
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
        toJson(event.eventData()),
        event.metaData()
        );
    return eventEntity;
  }

  private int calculateNextVersion(String streamId) {
    return (int) repo.countByStreamId(streamId);
  }

  private String toJson(D eventData) {
    try {
      return mapper.writeValueAsString(eventData);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Can not convert event data object to JSON", e);
    }
  }

  private Event<D> toEvent(EventJpaEntity eventEntity) {
    String eventTypeKey = eventEntity.getEventType() + eventEntity.getEventTypeVersion();
    try {
      D data = mapper.readValue(eventEntity.getData(),
          eventTypeToClassMap.get(eventTypeKey));
      return new Event<>(
          eventEntity.getStreamId(),
          eventEntity.getEventType(),
          eventEntity.getEventTypeVersion(),
          data,
          eventEntity.getMeta());
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Can not parse event data (JSON) from DB", e);
    }
  }
}
