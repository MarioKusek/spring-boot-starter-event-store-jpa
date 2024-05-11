package hr.fer.eventstore.base;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public abstract class Projection<S, E> {
  public abstract S initialState();
  public abstract S update(S currentState, E event);

  public S fold(S initialState, List<E> events) {
    S currentState = initialState;
    for(E e: events)
      currentState = update(currentState, e);
    return currentState;
  }

  public S fold(List<E> events) {
    return fold(initialState(), events);
  }

  public static <S, E> Projection<S, E> create(Supplier<S> initFn, BiFunction<S, E, S> updateFn) {
    Projection<S, E> p = new Projection<>() {

      @Override
      public S initialState() {
        return initFn.get();
      }

      @Override
      public S update(S currentState, E event) {
        return updateFn.apply(currentState, event);
      }
    };
    return p;
  }
}
