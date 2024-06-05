package hr.fer.icecream_truck;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import hr.fer.event.Event;
import hr.fer.event.Projection;
import hr.fer.event.StreamId;
import hr.fer.event.ddd.Result;
import hr.fer.event.store.EventMapper;
import hr.fer.event.store.EventStore;
import hr.fer.event.store.jpa.EventRepository;
import hr.fer.event.store.jpa.EventStoreDB;
import hr.fer.icecream_truck.commands.TruckCommand;
import hr.fer.icecream_truck.events.TruckEventData;

public class TruckService {
  TruckEventFactory factory;
  EventStore<TruckEventData> store;

  public TruckService(Function<EventMapper<TruckEventData>, EventStore<TruckEventData>> createNewStore) {
    factory = new TruckEventFactory();
    EventMapper<TruckEventData> mapper = factory.getMapper();
    store = createNewStore.apply(mapper);
  }

  public TruckService(EventRepository repo) {
    factory = new TruckEventFactory();
    store = new EventStoreDB<>(repo, factory.getMapper());
  }

  public StreamId createTruck(Map<String, String> metaData) {
    Event<TruckEventData> truckCreated = factory.createTruck(metaData);
    store.append(truckCreated);
    return truckCreated.streamId();
  }

  public void handle(TruckCommand command) {
    List<Event<TruckEventData>> events = store.getAllEvents(command.truckId());
    Truck truck = new Truck(events);

    Result<? extends RuntimeException, Event<TruckEventData>> result = truck.handle(command);
    store.appendAll(result.events());
    if(result.isError())
      throw result.exception();
  }

  // just for debugging
  public List<Event<TruckEventData>> getAllEvents(StreamId truckId) {
    return store.getAllEvents(truckId);
  }

  public <T> T getReport(StreamId truckId, Projection<T, TruckEventData> projection) {
     List<Event<TruckEventData>> events = store.getAllEvents(truckId);
     return projection.fold(events);
  }

  public <T> T getReport(Projection<T, TruckEventData> projection) {
    List<Event<TruckEventData>> events = store.getAllEventsStreamIdPrefixIs("truck");
    return projection.fold(events);
  }
}
