package hr.fer.event.store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import hr.fer.event.Event;
import hr.fer.event.StreamId;
import hr.fer.event.store.EventMapper.ClassTriple;

class EventStoreInMemoryTest {
  EventStore<Object> store;

  @BeforeEach
  void setup() {
    List<ClassTriple> types = List.of(
        EventMapper.classTriple("string", 1, String.class),
        EventMapper.classTriple("int", 1, Integer.class)
        );
    EventMapper<Object> mapper = new EventMapper<>(types);
    store = new EventStoreInMemory<>(mapper);
  }

  @Test
  void noEventsAfterInitialization() throws Exception {
    assertThat(store.getAllEvents()).isEmpty();
  }

  @Test
  void appendOneEvent() throws Exception {
    int version = store.append(createEvent(StreamId.ofValue("notImportant"), "e1"));

    assertThat(version).isEqualTo(1);
    assertThat(store.getAllEvents())
      .extracting("eventData", "version")
      .containsExactly(tuple("e1", 1));
  }

  @Test
  void appendMoreEvents() throws Exception {
    StreamId streamId = StreamId.ofValue("someStreamId");

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
  void getEvent() throws Exception {
    StreamId streamId = StreamId.ofValue("user-mkusek");
    store.append(createEvent(streamId, "e1"));
    store.append(createEvent(streamId, "e2"));
    store.append(createEvent(streamId, "e3"));

    final Optional<Event<Object>> optional = store.getEvent(streamId, 2);

    assertThat(optional).isPresent();
    assertThat(optional.get().eventData()).isEqualTo("e2");
  }

  @Test
  void getNotExistingEvent() throws Exception {
    StreamId streamId = StreamId.ofValue("user-mkusek");
    store.append(createEvent(streamId, "e1"));
    store.append(createEvent(streamId, "e2"));
    store.append(createEvent(streamId, "e3"));

    final Optional<Event<Object>> optional = store.getEvent(streamId, 5);

    assertThat(optional).isEmpty();
  }

  @Test
  void getEventsFromVersion() throws Exception {
    StreamId streamId = StreamId.ofValue("user-mkusek");
    store.append(createEvent(streamId, "e1"));
    store.append(createEvent(streamId, "e2"));
    store.append(createEvent(streamId, "e3"));

    assertThat(store.getAllEventsFromVersion(streamId, 2))
      .extracting("eventData", "version")
      .containsExactly(tuple("e2", 2), tuple("e3", 3));
  }

  @Test
  void getEventsWithStreamIdPrefix() throws Exception {
    store.append(createEvent(StreamId.ofValue("user-mkusek"), "e1"));
    store.append(createEvent(StreamId.ofValue("user-pperic"), "e2"));
    store.append(createEvent(StreamId.ofValue("user-mkusek"), "e3"));
    store.append(createEvent(StreamId.ofValue("truck-2456"), "e4"));

    assertThat(store.getAllEventsStreamIdPrefixIs("user"))
      .extracting("eventData")
      .containsExactlyInAnyOrder("e1", "e2", "e3");
    assertThat(store.getAllEventsStreamIdPrefixIs("truck"))
      .extracting("eventData")
      .containsExactlyInAnyOrder("e4");
  }

  @Test
  void getEventsWithStreamIdPrefixStartsWith() throws Exception {
    store.append(createEvent(StreamId.ofValue("user-mkusek"), "e1"));
    store.append(createEvent(StreamId.ofValue("user-pperic"), "e2"));
    store.append(createEvent(StreamId.ofValue("user-mkusek"), "e3"));
    store.append(createEvent(StreamId.ofValue("truck-2456"), "e4"));

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
    store.append(StreamId.ofValue("user-pperic"), Integer.valueOf(2));
    store.append(StreamId.ofValue("user-mkusek"), "e3");
    store.append(StreamId.ofValue("truck-2456"), 4);

    assertThat(store.getAllEventsForEventDataClass(String.class))
      .extracting("eventData")
      .containsExactlyInAnyOrder("e1", "e3");
    assertThat(store.getAllEventsForEventDataClass(Integer.class))
      .extracting("eventData")
      .containsExactlyInAnyOrder(Integer.valueOf(2), Integer.valueOf(4));
  }

  private <D> Event<D> createEvent(StreamId streamId, D data) {
    return Event.of(streamId, null, 0, data, null);
  }

}
