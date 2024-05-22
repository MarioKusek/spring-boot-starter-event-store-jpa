package hr.fer.eventstore.base;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import hr.fer.eventstore.base.EventMapper.ClassTriple;
import hr.fer.eventstore.base.jpa.EventJpaRepository;
import hr.fer.eventstore.base.jpa.TestContainersDbFixture;

@DataJpaTest(showSql = true, properties = {
    "logging.level.org.springframework.test.context.transaction=TRACE"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EventStoreDBTest extends TestContainersDbFixture {
  @Autowired
  EventJpaRepository repo;

  EventStoreDB<String> store;

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
    store.append(createEvent(data));

    List<Event<String>> allEvents = store.getAllEvents();
    assertThat(allEvents).hasSize(1);
    Event<String> event = allEvents.getFirst();
    assertThat(event.streamId()).isEqualTo("user-mkusek");
    assertThat(event.eventType()).isEqualTo("string");
    assertThat(event.eventTypeVersion()).isEqualTo(1);
    assertThat(event.eventData()).isEqualTo(data);
  }

  private Event<String> createEvent(String data) {
    return new Event<>("user-mkusek", "string", 1, data, Map.of());
  }

  @Test
  void appendMoreEvents() throws Exception {
    store.append(createEvent("e1"));
    store.append(createEvent("e2"));
    store.append(createEvent("e3"));

    assertThat(store.getAllEvents())
      .extracting("eventData")
      .containsExactly("e1", "e2", "e3");
  }

  @Test
  void evolveCommand() throws Exception {
    store.append(createEvent("e1"));

    store.evolve(events -> List.of("produced1", "produced2")
        .stream().map(this::createEvent).toList());

    assertThat(store.getAllEvents())
      .extracting("eventData")
      .containsExactly("e1", "produced1", "produced2");
  }

}
