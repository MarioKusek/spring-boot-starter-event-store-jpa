package hr.fer.event.ddd;

import java.util.Arrays;
import java.util.List;

import org.springframework.util.Assert;

public class Result<E extends Exception, T> {
  private final E exception;
  private final List<T> events;

  public Result(E exception, List<T> events) {
    this.exception = exception;
    this.events = events;
  }

  public  static <A extends Exception, B> Result<A, B> exception(A exception) {
    return new Result<>(exception, List.of());
  }

  public  static <A extends Exception, B> Result<A, B> exception(A exception, List<B> events) {
    Assert.notNull(events, "Result events can not be null.");
    return new Result<>(exception, events);
  }

  public  static <A extends Exception, B> Result<A, B> exception(A exception, B ...events) {
    Assert.notEmpty(events, "Result events can not be null.");
    return new Result<>(exception, Arrays.asList(events));
  }

  public  static <A extends Exception, B> Result<A, B> events(B ...events) {
    Assert.notEmpty(events, "Events can not be empty.");
    return new Result<>(null, Arrays.asList(events));
  }

  public  static <A extends Exception, B> Result<A, B> events(List<B> events) {
    Assert.notEmpty(events, "Events can not be empty.");
    return new Result<>(null, events);
  }

  public E exception() {
    return exception;
  }

  public boolean isError() {
    return exception != null;
  }

  public List<T> events() {
    return events;
  }

  public boolean hasEvents() {
    return !events.isEmpty();
  }

}
