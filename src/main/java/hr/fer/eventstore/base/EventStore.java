package hr.fer.eventstore.base;

import java.util.List;
import java.util.Map;

import hr.fer.eventstore.base.EventMapper.TypeVersion;

public abstract class EventStore<D> {
  private EventMapper<D> eventMapper;

  public EventStore(EventMapper<D> mapper) {
    eventMapper = mapper;
  }

  /**
   * Append event to store.
   *
   * If event version is -1 then it should be replaced with current version of the streamId
   *
   * @param event the event that should be saved
   */
  public abstract void append(Event<D> event);

  public void append(StreamId id, D eventData, Map<String, String> metaData) {
    @SuppressWarnings("unchecked")
    Class<? extends D> eventClass = (Class<? extends D>) eventData.getClass();
    TypeVersion vt = eventMapper.getEventTypeVersion(eventClass);
    append(Event.of(id, vt.type(), vt.version(), eventData, metaData));
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
  // TODO sve događaje čiji streamID.prefix počinje s nekim tekstom
  //public abstract List<Event<D>> getAllEventsStreamIdPrefixStartsWith(String streamIdPrefixStartsWith, int fromVersion);
  // TODO za sve događaje i pojedine tipove događaja (klase od eventData)
  //public abstract List<Event<D>> getAllEventsForEventDataClass(Class<?> ...eventDataClasses);

}