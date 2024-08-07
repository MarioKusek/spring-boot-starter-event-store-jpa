package hr.fer.event.store;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import hr.fer.event.Event;
import hr.fer.event.StreamId;
import hr.fer.event.store.EventMapper.TypeVersion;

public abstract class EventStore<D> {
  protected final EventMapper<D> eventMapper;

  public EventStore(EventMapper<D> mapper) {
    eventMapper = mapper;
  }

  /**
   * Append event to store.
   *
   * If event version is -1 then it should be replaced with current version of the streamId
   *
   * @param event the event that should be saved
   * @return version of event in stream
   */
  public abstract int append(Event<D> event);

  public int append(StreamId id, D eventData, Map<String, String> metaData) {
    @SuppressWarnings("unchecked")
    Class<? extends D> eventClass = (Class<? extends D>) eventData.getClass();
    TypeVersion vt = eventMapper.getEventTypeVersion(eventClass);
    return append(Event.of(id, vt.type(), vt.version(), eventData, metaData));
  }

  public void appendAll(List<Event<D>> newEvents) {
    for(Event<D> event: newEvents) {
      append(event);
    }
  }

  public int append(StreamId id, D eventData) {
    return append(id, eventData, Map.of());
  }

  public abstract List<Event<D>> getAllEvents();
  public abstract List<Event<D>> getAllEvents(StreamId streamId);

  /**
   * Returns specific event by streamId and version.
   *
   * @param streamId
   * @param version
   * @return
   */
  public abstract Optional<Event<D>> getEvent(StreamId streamId, int version);

  /**
   * Returns all events from version.
   *
   * @param streamId
   * @param fromVersion
   * @return
   */
  public abstract List<Event<D>> getAllEventsFromVersion(StreamId streamId, int fromVersion);

  /**
   * Returns all events whose streamId has prefix.
   *
   * @param streamIdPrefix
   * @return
   */
  public abstract List<Event<D>> getAllEventsStreamIdPrefixIs(String streamIdPrefix);

  /**
   * Returns all events whose streamId has prefix that starts with.
   * @param streamIdPrefixStartsWith
   * @return
   */
  public abstract List<Event<D>> getAllEventsStreamIdPrefixStartsWith(String streamIdPrefixStartsWith);

  /**
   * Returns all events with event type
   *
   * @param eventDataClasses
   * @return
   */
  public abstract List<Event<D>> getAllEventsForEventDataClass(Class<? extends D> ...eventDataClasses);

}