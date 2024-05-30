package hr.fer.eventstore.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import hr.fer.eventstore.base.StreamId;
import io.hypersistence.tsid.TSID.Factory;

@DataJpaTest(showSql = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EventRepositoryTest extends TestContainersDbFixture {

  @Autowired
  private EventRepository repo;

  @Configuration
  @ComponentScan(basePackages = {"hr.fer.eventstore"})
  @EnableAutoConfiguration
  static class MyTestConfig {

  }

  @Test
  void saveAndLoad() {
    EventJpaEntity event = new EventJpaEntity();
    long id = Factory.getTsid().toLong();
    event.setId(id);
    event.setData("\"test json\"");
    Map<String, String> meta = Map.of(
        "created", LocalDateTime.now().toString(),
        "ip", "127.0.0.1",
        "userId", "mario");
    event.setMeta(meta);
    event.setEventType("string");
    event.setEventTypeVersion(1);
    event.setStreamId("user-login");
    event.setVersion(1);

    repo.save(event);

    var loadedEvent = repo.findById(event.getId()).get();
    assertThat(loadedEvent.getData()).isEqualTo("\"test json\"");
    assertThat(loadedEvent.getMeta()).isEqualTo(meta);
    assertThat(loadedEvent.getStreamId()).isEqualTo("user-login");
    assertThat(loadedEvent.getVersion()).isEqualTo(1);
    assertThat(loadedEvent.getEventType()).isEqualTo("string");
    assertThat(loadedEvent.getEventTypeVersion()).isEqualTo(1);
  }

  @Test
  void countByStreamId() throws Exception {
    repo.saveAll(List.of(
        createStubEventWithStreamId("sid1", 1),
        createStubEventWithStreamId("sid1", 2),
        createStubEventWithStreamId("sid2", 1)
    ));

    assertThat(repo.countByStreamId("sid1")).isEqualTo(2);
    assertThat(repo.countByStreamId("sid2")).isEqualTo(1);
  }

  @Test
  void findEventsByStreamId() throws Exception {
    repo.saveAll(List.of(
        createStubEventWithStreamIdAndData("sid1", 1, "\"d1\""),
        createStubEventWithStreamIdAndData("sid1", 2, "\"d2\""),
        createStubEventWithStreamIdAndData("sid2", 1, "\"d3\""),
        createStubEventWithStreamIdAndData("sid1", 3, "\"d4\""),
        createStubEventWithStreamIdAndData("sid2", 2, "\"d5\"")
        ));


    assertThat(repo.findAllByStreamId(StreamId.of("sid1")))
      .hasSize(3)
      .extracting("data")
      .containsExactly("\"d1\"", "\"d2\"", "\"d4\"");

    assertThat(repo.findAllByStreamId(StreamId.of("sid2")))
      .hasSize(2)
      .extracting("data")
      .containsExactly("\"d3\"", "\"d5\"");
  }

  private EventJpaEntity createStubEventWithStreamId(String streamId, int version) {
    return createStubEventWithStreamIdAndData(streamId, version, "\"test json\"");
  }

  private EventJpaEntity createStubEventWithStreamIdAndData(String streamId, int version, String data) {
    EventJpaEntity event = new EventJpaEntity();
    long id = Factory.getTsid().toLong();
    event.setId(id);
    event.setData(data);
    Map<String, String> meta = Map.of(
        "created", LocalDateTime.now().toString(),
        "ip", "127.0.0.1",
        "userId", "mario");
    event.setMeta(meta);
    event.setEventType("string");
    event.setEventTypeVersion(1);
    event.setStreamId(streamId);
    event.setVersion(version);

    return event;
  }

}
