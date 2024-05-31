package hr.fer.eventstore.base;

import java.util.List;
import java.util.Map;

public record Event<D>(
    StreamId streamId,
    int version, // version inside streamId
    String eventType,
    int eventTypeVersion,
    D eventData,
    Map<String,String> metaData) {

  public Event<D> copyWithVersion(int newVersion) {
    return new Event<>(streamId, newVersion, eventType, eventTypeVersion, eventData, metaData);
  }

  public static <D> List<Event<D>> createList(StreamId id, String eventType, int eventTypeVersion,  List<D> data,  Map<String,String> metaData) {
    return data.stream()
        .map(d -> new Event<>(id, -1, eventType, eventTypeVersion, d, metaData))
        .toList();
  }

  public static <D> Event<D> of(StreamId id, String eventType, int eventTypeVersion, D data, Map<String,String> metaData) {
    return new Event<>(id, -1, eventType, eventTypeVersion, data, metaData);
  }
}
