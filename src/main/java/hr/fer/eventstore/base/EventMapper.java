package hr.fer.eventstore.base;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import hr.fer.eventstore.base.jpa.EventJpaEntity;

public class EventMapper<D> {
  private static final ObjectMapper mapper = new ObjectMapper();

  public static record TypeVersion(String type, int version) {}

  public static record ClassTriple(String eventType, int eventTypeVersion, Class<?> clazz) {
    // TODO prebaciti u vanjsku klasu
    public static ClassTriple classTriple(String eventType, int eventTypeVersion, Class<?> clazz) {
      return new ClassTriple(eventType, eventTypeVersion, clazz);
    }

    public TypeVersion typeVersion() {
      return new TypeVersion(eventType, eventTypeVersion);
    }
  }

  private Map<TypeVersion, Class<? extends D>> eventTypeVersonToClassMap;
  private Map<Class<? extends D>, TypeVersion> classToEventTypeVersionMap;

  public EventMapper(List<ClassTriple> typeList) {
    this.classToEventTypeVersionMap = new HashMap<>();
    this.eventTypeVersonToClassMap = new HashMap<>();
    for(var ct: typeList) {
      classToEventTypeVersionMap.put((Class<? extends D>) ct.clazz(), ct.typeVersion());
      eventTypeVersonToClassMap.put(ct.typeVersion(), (Class<? extends D>) ct.clazz());
    }
  }

  public TypeVersion getEventTypeVersion(Class<? extends D> eventClass) {
    return classToEventTypeVersionMap.get(eventClass);
  }

  public String toJson(D eventData) {
    try {
      return mapper.writeValueAsString(eventData);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Can not convert event data object to JSON", e);
    }
  }

  public Event<D> toEvent(EventJpaEntity eventEntity) {
    TypeVersion eventTypeKey = new TypeVersion(eventEntity.getEventType(), eventEntity.getEventTypeVersion());
    try {
      D data = mapper.readValue(eventEntity.getData(),
          eventTypeVersonToClassMap.get(eventTypeKey));
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
