package hr.fer.event.ddd;

import java.util.List;

import hr.fer.event.Event;

public abstract class EventSourcesAggregate<D, C> {
  public EventSourcesAggregate(List<Event<D>> events) {
    validateEvents(events);
    evolveState(events);
  }

  protected void validateEvents(List<Event<D>> events) {
    if(events.isEmpty())
      throw new IllegalStateException("Aggregate not created.");
  }

  protected void evolveState(List<Event<D>> events) {
    for(var event: events) {
      evolveState(event);
    }
  }

  protected abstract void evolveState(Event<D> event);

  public abstract Result<? extends RuntimeException, Event<D>> handle(C command);
}
