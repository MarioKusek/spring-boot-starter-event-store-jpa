package hr.fer.eventstore.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import hr.fer.eventstore.base.EventMapper.ClassTriple;

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

  @Test
  void getEventsWithStreamIdPrefix() throws Exception {
    store.append(createEvent(StreamId.of("user-mkusek"), "e1"));
    store.append(createEvent(StreamId.of("user-pperic"), "e2"));
    store.append(createEvent(StreamId.of("user-mkusek"), "e3"));
    store.append(createEvent(StreamId.of("truck-2456"), "e4"));

    assertThat(store.getAllEventsStreamIdPrefixIs("user"))
      .extracting("eventData")
      .containsExactlyInAnyOrder("e1", "e2", "e3");
    assertThat(store.getAllEventsStreamIdPrefixIs("truck"))
      .extracting("eventData")
      .containsExactlyInAnyOrder("e4");
  }

  @Test
  void getEventsWithStreamIdPrefixStartsWith() throws Exception {
    store.append(createEvent(StreamId.of("user-mkusek"), "e1"));
    store.append(createEvent(StreamId.of("user-pperic"), "e2"));
    store.append(createEvent(StreamId.of("user-mkusek"), "e3"));
    store.append(createEvent(StreamId.of("truck-2456"), "e4"));

    assertThat(store.getAllEventsStreamIdPrefixStartsWith("u"))
      .extracting("eventData")
      .containsExactlyInAnyOrder("e1", "e2", "e3");
    assertThat(store.getAllEventsStreamIdPrefixStartsWith("t"))
      .extracting("eventData")
      .containsExactlyInAnyOrder("e4");
  }

  @Test
  void getEventsWithEventDataClass() throws Exception {
    store.append(StreamId.of("user-mkusek"), "e1");
    store.append(StreamId.of("user-pperic"), Integer.valueOf(2));
    store.append(StreamId.of("user-mkusek"), "e3");
    store.append(StreamId.of("truck-2456"), 4);

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
