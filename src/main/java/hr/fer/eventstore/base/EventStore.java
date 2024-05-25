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

  public void append(String streamId, D eventData, Map<String, String> metaData) {
    Class<? extends D> eventClass = (Class<? extends D>) eventData.getClass();
    TypeVersion vt = eventMapper.getEventTypeVersion(eventClass);
    append(new Event<>(streamId, vt.type(), vt.version(), eventData, metaData));
  }

  public void appendAll(List<Event<D>> newEvents) {
    for(Event<D> event: newEvents) {
      append(event);
    }
  }

  public void append(String streamId, D eventData) {
    append(streamId, eventData, Map.of());
  }

  public abstract List<Event<D>> getAllEvents();
  public abstract List<Event<D>> getAllEvents(String streamId);

  public void evolve(EventProducer<D> eventProducer) {
    List<Event<D>> allEvents = getAllEvents();
    List<Event<D>> produced = eventProducer.produce(allEvents);
    appendAll(produced);
  }

  public void evolve(String streamId, EventProducer<D> eventProducer) {
    List<Event<D>> allStreamEvents = getAllEvents(streamId);
    List<Event<D>> produced = eventProducer.produce(allStreamEvents);
    appendAll(produced);
  }
}