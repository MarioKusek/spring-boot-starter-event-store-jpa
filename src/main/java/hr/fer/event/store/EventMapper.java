package hr.fer.event.store;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class EventMapper<D> {
  private static final ObjectMapper mapper = JsonMapper.builder()
      .addModule(new JavaTimeModule())
      .build();

  public static record TypeVersion(String type, int version) {}

  public static record ClassTriple(String eventType, int eventTypeVersion, Class<?> clazz) {
    public TypeVersion typeVersion() {
      return new TypeVersion(eventType, eventTypeVersion);
    }
  }

  private Map<TypeVersion, Class<? extends D>> eventTypeVersonToClassMap;
  private Map<Class<? extends D>, TypeVersion> classToEventTypeVersionMap;

  @SuppressWarnings("unchecked")
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

  public D toEventData(String jsonData, TypeVersion typeVersion) {
    TypeVersion eventTypeKey = typeVersion;
    try {
      D data = mapper.readValue(jsonData,
          eventTypeVersonToClassMap.get(eventTypeKey));
      return data;
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Can not parse event data (JSON) from DB", e);
    }
  }

  public static ClassTriple classTriple(String eventType, int eventTypeVersion, Class<?> clazz) {
    return new ClassTriple(eventType, eventTypeVersion, clazz);
  }

}
