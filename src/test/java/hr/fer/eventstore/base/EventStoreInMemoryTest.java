package hr.fer.eventstore.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import org.junit.jupiter.api.Test;

class EventStoreInMemoryTest {
  EventStore<String> store = new EventStoreInMemory<>(null);

  @Test
  void noEventsAfterInitialization() throws Exception {
    assertThat(store.getAllEvents()).isEmpty();
  }

  @Test
  void appendOneEvent() throws Exception {
    store.append(createEvent(StreamId.of("notImportant"), "e1"));

    assertThat(store.getAllEvents())
      .extracting("eventData", "version")
      .containsExactly(tuple("e1", 1));
  }

  @Test
  void appendMoreEvents() throws Exception {
    StreamId streamId = StreamId.of("someStreamId");

    store.append(createEvent(streamId, "e1"));
    store.append(createEvent(streamId, "e2"));
    store.append(createEvent(streamId, "e3"));

    assertThat(store.getAllEvents())
      .extracting("eventData", "version")
      .containsExactly(
          tuple("e1", 1),
          tuple("e2", 2),
          tuple("e3", 3));
  }

  @Test
  void getEventsFromVersion() throws Exception {
    StreamId streamId = StreamId.of("user-mkusek");
    store.append(createEvent(streamId, "e1"));
    store.append(createEvent(streamId, "e2"));
    store.append(createEvent(streamId, "e3"));

    assertThat(store.getAllEventsFromVersion(streamId, 2))
      .extracting("eventData", "version")
      .containsExactly(tuple("e2", 2), tuple("e3", 3));
  }


  private Event<String> createEvent(StreamId streamId, String string) {
    return Event.of(streamId, null, 0, string, null);
  }

}
