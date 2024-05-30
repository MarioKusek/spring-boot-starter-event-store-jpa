package hr.fer.eventstore.base;

import java.util.List;
import java.util.Map;

import hr.fer.eventstore.base.EventMapper.TypeVersion;

public abstract class EventStore<D> {
  private EventMapper<D> eventMapper;

  public EventStore(EventMapper<D> mapper) {
    eventMapper = mapper;
  }

  public abstract void append(Event<D> eventData);

  public void append(StreamId id, D eventData, Map<String, String> metaData) {
    @SuppressWarnings("unchecked")
    Class<? extends D> eventClass = (Class<? extends D>) eventData.getClass();
    TypeVersion vt = eventMapper.getEventTypeVersion(eventClass);
    append(new Event<>(id, vt.type(), vt.version(), eventData, metaData));
  }

  public void appendAll(List<Event<D>> newEvents) {
    for(Event<D> event: newEvents) {
      append(event);
    }
  }

  public void append(StreamId id, D eventData) {
    append(id, eventData, Map.of());
  }

  public abstract List<Event<D>> getAllEvents();
  public abstract List<Event<D>> getAllEvents(StreamId streamId);
}