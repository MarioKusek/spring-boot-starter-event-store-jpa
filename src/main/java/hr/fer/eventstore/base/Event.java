package hr.fer.eventstore.base;

import java.util.List;
import java.util.Map;

public record Event<D>(
    String streamId,
    String eventType,
    int eventTypeVersion,
    D eventData,
    Map<String,String> metaData) {


  public static <D> List<Event<D>> createList(String streamId, String eventType, int eventTypeVersion,  List<D> data,  Map<String,String> metaData) {
    return data.stream()
        .map(d -> new Event<>(streamId, eventType, eventTypeVersion, d, metaData))
        .toList();
  }
}
