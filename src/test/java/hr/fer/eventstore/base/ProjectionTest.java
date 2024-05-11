package hr.fer.eventstore.base;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

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
      public String update(String currentState, String event) {
        return currentState + "|" + event;
      }
    };

    assertThat(p.fold(List.of("e1", "e2", "e3"))).isEqualTo("|e1|e2|e3");
  }


  @Test
  void projectionWithLambdas() throws Exception {
    Projection<String, String> p = Projection.create(
        () -> "",
        (String state, String event) -> state + "|" + event);

    assertThat(p.fold(List.of("e1", "e2", "e3"))).isEqualTo("|e1|e2|e3");
  }

}
