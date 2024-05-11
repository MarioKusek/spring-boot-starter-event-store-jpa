package hr.fer.eventstore.base;

import java.util.Map;

public record Event<D>(
    String streamId,
    String eventType,
    int eventTypeVersion,
    D eventData,
    Map<String,String> metaData) {
}
