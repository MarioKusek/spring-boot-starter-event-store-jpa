package hr.fer.icecream_truck;

import java.util.List;
import java.util.Map;

import org.springframework.util.Assert;

import hr.fer.eventstore.base.Event;
import hr.fer.eventstore.base.EventStore;
import hr.fer.eventstore.base.Projection;
import hr.fer.eventstore.base.StreamId;
import hr.fer.eventstore.jpa.EventRepository;
import hr.fer.eventstore.jpa.EventStoreDB;
import hr.fer.icecream_truck.commands.CreateTruck;
import hr.fer.icecream_truck.commands.RestockFlavour;
import hr.fer.icecream_truck.commands.SellFlavour;
import hr.fer.icecream_truck.commands.TruckCommand;
import hr.fer.icecream_truck.events.TruckEventData;

public class TruckService {
  TruckEventFactory factory;
  EventStore<TruckEventData> store;

  public TruckService(EventRepository repo) {
    factory = new TruckEventFactory();
    store = new EventStoreDB<>(repo, factory.getMapper());
  }

  public StreamId createTruck(CreateTruck command) {
    Event<TruckEventData> truckCreated = factory.createTruck(command.metaData());
    store.append(truckCreated);
    return truckCreated.streamId();
  }

  public void handle(TruckCommand command) {
    switch (command) {
    case CreateTruck create -> throw new IllegalArgumentException("Creating truck should use createTruck method.");
    case RestockFlavour restock -> restockFlavour(restock);
    case SellFlavour sell -> sell(sell);
    };
  }

  private void sell(SellFlavour sellCommand) {
    StreamId truckId = sellCommand.truckId();
    List<Event<TruckEventData>> events = store.getAllEvents(truckId);
    Assert.notEmpty(events, "There sould be at least one event");

    Truck truck = new Truck(events);
    FlavourName flavour = sellCommand.flavour();
    Map<String, String> metaData = sellCommand.metaData();
    Amount inStock = truck.getFlavourState(flavour);

    var newEvents = switch(inStock.value()) {
      case 0 -> List.of(factory.flavourWasNotInStock(truckId, flavour, metaData));
      case 1 -> List.of(factory.flavourSold(truckId, flavour, metaData), factory.flavourWentOutOfStock(truckId, flavour, metaData));
      default -> List.of(factory.flavourSold(truckId, flavour, metaData));
    };

    store.appendAll(newEvents);
  }

  private void restockFlavour(RestockFlavour restockCommand) {
    store.append(factory.flavourRestocked(restockCommand.truckId(), restockCommand.flavour(), restockCommand.amount(), restockCommand.metaData()));
  }

  // just for debugging
  public List<Event<TruckEventData>> getAllEvents(StreamId truckId) {
    return store.getAllEvents(truckId);
  }

   public <T> T getReport(StreamId truckId, Projection<T, TruckEventData> projection) {
     List<Event<TruckEventData>> events = store.getAllEvents(truckId);
     return projection.fold(events);
  }
}
