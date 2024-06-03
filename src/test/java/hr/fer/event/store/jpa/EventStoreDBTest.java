package hr.fer.event.store.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import hr.fer.event.Event;
import hr.fer.event.StreamId;
import hr.fer.event.store.EventMapper;
import hr.fer.event.store.EventMapper.ClassTriple;

@DataJpaTest(showSql = true, properties = {
    "logging.level.org.springframework.test.context.transaction=TRACE"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EventStoreDBTest extends TestContainersDbFixture {
  @Autowired
  EventRepository repo;

  EventStoreDB<Object> store;

  @Configuration
  @ComponentScan(basePackages = {"hr.fer.event.store"})
  @EnableAutoConfiguration
  static class MyTestConfig {

  }

  @BeforeEach
  void setup() {
    repo.deleteAll();
    List<ClassTriple> types = List.of(
        EventMapper.classTriple("string", 1, String.class),
        EventMapper.classTriple("int", 1, Integer.class)
        );
    EventMapper<Object> mapper = new EventMapper<>(types);
    store = new EventStoreDB<>(repo, mapper);
  }

  @Test
  void noEventsAfterInitialization() throws Exception {
    assertThat(store.getAllEvents()).isEmpty();
  }

  @Test
  void appendOneEvent() throws Exception {
    String data = "e1";
    store.append(StreamId.ofValue("user-mkusek"), data);

    List<Event<Object>> allEvents = store.getAllEvents();
    assertThat(allEvents).hasSize(1);
    Event<Object> event = allEvents.getFirst();
    assertThat(event.streamId()).isEqualTo(StreamId.ofValue("user-mkusek"));
    assertThat(event.version()).isEqualTo(1);
    assertThat(event.eventType()).isEqualTo("string");
    assertThat(event.eventTypeVersion()).isEqualTo(1);
    assertThat(event.eventData()).isEqualTo(data);
  }

  private <D> Event<D> createEvent(D data, int version) {
    return new Event<>(StreamId.ofValue("user-mkusek"), version, "string", 1, data, Map.of());
  }

  @Test
  void appendMoreEvents() throws Exception {
    store.append(createEvent("e1", 1));
    store.append(createEvent("e2", 2));
    store.append(createEvent("e3", 3));

    assertThat(store.getAllEvents())
      .extracting("eventData")
      .containsExactly("e1", "e2", "e3");
  }

  @Test
  void appendMoreEventsInDifferentStreams() throws Exception {
    store.append(StreamId.ofValue("user-mkusek"), "e1");
    store.append(StreamId.ofValue("user-pperic"), "e2");
    store.append(StreamId.ofValue("user-mkusek"), "e3");

    assertThat(store.getAllEvents(StreamId.ofValue("user-mkusek")))
      .extracting("eventData", "version")
      .containsExactly(tuple("e1", 1), tuple("e3", 2));
    assertThat(store.getAllEvents(StreamId.ofValue("user-pperic")))
      .extracting("eventData", "version")
      .containsExactly(tuple("e2", 1));
  }

  @Test
  void getEventsFromVersion() throws Exception {
    StreamId streamId = StreamId.ofValue("user-mkusek");
    store.append(streamId, "e1");
    store.append(streamId, "e2");
    store.append(streamId, "e3");

    assertThat(store.getAllEventsFromVersion(streamId, 2))
      .extracting("eventData", "version")
      .containsExactly(tuple("e2", 2), tuple("e3", 3));
  }

  @Test
  void getEventsWithStreamIdPrefix() throws Exception {
    store.append(StreamId.ofValue("user-mkusek"), "e1");
    store.append(StreamId.ofValue("user-pperic"), "e2");
    store.append(StreamId.ofValue("user-mkusek"), "e3");
    store.append(StreamId.ofValue("truck-2456"), "e4");

    assertThat(store.getAllEventsStreamIdPrefixIs("user"))
      .extracting("eventData")
      .containsExactlyInAnyOrder("e1", "e2", "e3");
    assertThat(store.getAllEventsStreamIdPrefixIs("truck"))
      .extracting("eventData")
      .containsExactlyInAnyOrder("e4");
  }

  @Test
  void getEventsWithStreamIdPrefixStartsWith() throws Exception {
    store.append(StreamId.ofValue("user-mkusek"), "e1");
    store.append(StreamId.ofValue("user-pperic"), "e2");
    store.append(StreamId.ofValue("user-mkusek"), "e3");
    store.append(StreamId.ofValue("truck-2456"), "e4");

    assertThat(store.getAllEventsStreamIdPrefixStartsWith("u"))
      .extracting("eventData")
      .containsExactlyInAnyOrder("e1", "e2", "e3");
    assertThat(store.getAllEventsStreamIdPrefixStartsWith("t"))
      .extracting("eventData")
      .containsExactlyInAnyOrder("e4");
  }

  @Test
  void getEventsWithEventDataClass() throws Exception {
    store.append(StreamId.ofValue("user-mkusek"), "e1");
    store.append(StreamId.ofValue("user-pperic"), 2);
    store.append(StreamId.ofValue("user-mkusek"), "e3");
    store.append(StreamId.ofValue("truck-2456"), 4);

    assertThat(store.getAllEventsForEventDataClass(String.class))
      .extracting("eventData")
      .containsExactlyInAnyOrder("e1", "e3");
    assertThat(store.getAllEventsForEventDataClass(Integer.class))
      .extracting("eventData")
      .containsExactlyInAnyOrder(2, 4);
    assertThat(store.getAllEventsForEventDataClass(String.class, Integer.class))
      .extracting("eventData")
      .containsExactlyInAnyOrder("e1", "e3", 2, 4);
  }

}
