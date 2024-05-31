package hr.fer.eventstore.jpa;

import static org.assertj.core.api.Assertions.assertThat;

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

import hr.fer.eventstore.base.Event;
import hr.fer.eventstore.base.EventMapper;
import hr.fer.eventstore.base.EventMapper.ClassTriple;
import hr.fer.eventstore.base.StreamId;

@DataJpaTest(showSql = true, properties = {
    "logging.level.org.springframework.test.context.transaction=TRACE"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EventStoreDBTest extends TestContainersDbFixture {
  @Autowired
  EventRepository repo;

  EventStoreDB<String> store;

  @Configuration
  @ComponentScan(basePackages = {"hr.fer.eventstore"})
  @EnableAutoConfiguration
  static class MyTestConfig {

  }

  @BeforeEach
  void setup() {
    repo.deleteAll();
    List<ClassTriple> types = List.of(EventMapper.classTriple("string", 1, String.class));
    EventMapper<String> mapper = new EventMapper<>(types);
    store = new EventStoreDB<>(repo, mapper);
  }

  @Test
  void noEventsAfterInitialization() throws Exception {
    assertThat(store.getAllEvents()).isEmpty();
  }

  @Test
  void appendOneEvent() throws Exception {
    String data = "e1";
    store.append(StreamId.of("user-mkusek"), data);

    List<Event<String>> allEvents = store.getAllEvents();
    assertThat(allEvents).hasSize(1);
    Event<String> event = allEvents.getFirst();
    assertThat(event.streamId()).isEqualTo(StreamId.of("user-mkusek"));
    assertThat(event.version()).isEqualTo(1);
    assertThat(event.eventType()).isEqualTo("string");
    assertThat(event.eventTypeVersion()).isEqualTo(1);
    assertThat(event.eventData()).isEqualTo(data);
  }

  private Event<String> createEvent(String data, int version) {
    return new Event<>(StreamId.of("user-mkusek"), version, "string", 1, data, Map.of());
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
    store.append(StreamId.of("user-mkusek"), "e1");
    store.append(StreamId.of("user-pperic"), "e2");
    store.append(StreamId.of("user-mkusek"), "e3");

    assertThat(store.getAllEvents(StreamId.of("user-mkusek")))
      .extracting("eventData")
      .containsExactly("e1", "e3");
    assertThat(store.getAllEvents(StreamId.of("user-pperic")))
      .extracting("eventData")
      .containsExactly("e2");
  }
}
