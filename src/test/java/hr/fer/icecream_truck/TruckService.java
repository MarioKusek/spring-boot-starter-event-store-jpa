package hr.fer.icecream_truck;

import java.util.Map;

import hr.fer.eventstore.base.Event;
import hr.fer.eventstore.base.EventStore;
import hr.fer.eventstore.base.StreamId;
import hr.fer.eventstore.jpa.EventRepository;
import hr.fer.eventstore.jpa.EventStoreDB;
import hr.fer.icecream_truck.events.TruckEventData;

public class TruckService {
  TruckEventFactory factory;
  EventStore<TruckEventData> store;

  public TruckService(EventRepository repo) {
    factory = new TruckEventFactory();
    store = new EventStoreDB<>(repo, factory.getMapper());
  }

  public StreamId createTruck(Map<String, String> metaData) {
    Event<TruckEventData> truckCreated = factory.createTruck(metaData);
    store.append(truckCreated);
    return truckCreated.streamId();
  }
}
