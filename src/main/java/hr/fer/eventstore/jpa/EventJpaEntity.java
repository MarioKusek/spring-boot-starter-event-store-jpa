package hr.fer.eventstore.jpa;

import java.util.Map;

import org.hibernate.annotations.Type;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "event_store")
public class EventJpaEntity {
  @Id
  private long id;

  private String streamId;
  private int version;

  private String eventType;
  private int eventTypeVersion;

  @Type(JsonType.class)
  private String data;

  @Type(JsonType.class)
  private Map<String, String> meta;

  public EventJpaEntity() {
  }

  public EventJpaEntity(long id, String streamId, int version, String eventType, int eventTypeVersion, String data,
      Map<String, String> meta) {
    this.id = id;
    this.streamId = streamId;
    this.version = version;
    this.eventType = eventType;
    this.eventTypeVersion = eventTypeVersion;
    this.data = data;
    this.meta = meta;
  }



  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getStreamId() {
    return streamId;
  }

  public void setStreamId(String streamId) {
    this.streamId = streamId;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public String getEventType() {
    return eventType;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  public int getEventTypeVersion() {
    return eventTypeVersion;
  }

  public void setEventTypeVersion(int eventTypeVersion) {
    this.eventTypeVersion = eventTypeVersion;
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }

  public Map<String, String> getMeta() {
    return meta;
  }

  public void setMeta(Map<String, String> meta) {
    this.meta = meta;
  }

}
