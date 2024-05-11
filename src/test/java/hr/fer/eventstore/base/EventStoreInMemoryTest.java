package hr.fer.eventstore.base;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class EventStoreInMemoryTest {
  EventStore<String> store = new EventStoreInMemory<>();

  @Test
  void noEventsAfterInitialization() throws Exception {
    assertThat(store.getAllEvents()).isEmpty();
  }

  @Test
  void appendOneEvent() throws Exception {
    store.append(createEvent("e1"));

    assertThat(store.getAllEvents())
      .extracting("eventData")
      .containsExactly("e1");
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

    store.evolve(events -> List.of("produced1", "produced2").stream().map(this::createEvent).toList());

    assertThat(store.getAllEvents())
      .extracting("eventData")
      .containsExactly("e1", "produced1", "produced2");
  }

  private Event<String> createEvent(String string) {
    return new Event<>(null, null, 0, string, null);
  }

}
