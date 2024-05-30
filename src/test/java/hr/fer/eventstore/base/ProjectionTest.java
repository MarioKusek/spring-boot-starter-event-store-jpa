package hr.fer.eventstore.base;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class ProjectionTest {

  @Test
  void foldFunction() {
    Projection<String, String> p = new Projection<>() {

      @Override
      public String initialState() {
        return "";
      }

      @Override
      public String update(String currentState, Event<String> event) {
        return currentState + "|" + event.eventData();
      }
    };

    List<Event<String>> events = Event.createList(StreamId.of("projection"), "e", 1, List.of("e1", "e2", "e3"), Map.of());
    assertThat(p.fold(events)).isEqualTo("|e1|e2|e3");
  }


  @Test
  void projectionWithLambdas() throws Exception {
    Projection<String, String> p = Projection.create(
        () -> "",
        (String state, Event<String> event) -> state + "|" + event.eventData());

    assertThat(p.fold(Event.createList(StreamId.of("perojection"), "e", 1, List.of("e1", "e2", "e3"), Map.of()))).isEqualTo("|e1|e2|e3");
  }

}
