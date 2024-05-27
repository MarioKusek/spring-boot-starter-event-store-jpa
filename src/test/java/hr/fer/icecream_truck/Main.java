package hr.fer.icecream_truck;

import java.util.Map;

import org.springframework.stereotype.Component;

import hr.fer.eventstore.base.Event;
import hr.fer.eventstore.base.EventStore;
import hr.fer.eventstore.jpa.EventJpaRepository;
import hr.fer.eventstore.jpa.EventStoreDB;
import hr.fer.icecream_truck.events.TruckEventData;
import jakarta.annotation.PostConstruct;

@Component
public class Main {
  private EventJpaRepository repo;

  public Main(EventJpaRepository repo) {
    this.repo = repo;
  }

  @PostConstruct
  void run() {
    basicExamples();
    evolveExample();
  }

  private void evolveExample() {
    System.out.println("==== evolve example");
    TruckEventFactory factory = new TruckEventFactory();
    EventStore<TruckEventData> store = new EventStoreDB<>(repo, factory.getMapper());
    Map<String, String> notImportantMetaData = Map.of();

    // creating truck
    Event<TruckEventData> truckCreated = factory.createTruck(notImportantMetaData);
    String streamId = truckCreated.streamId();
    store.append(truckCreated);
    System.out.println("Truck created with streamId: " + streamId);

    store.append(factory.flavourRestocked(streamId, "vanilija", 1, notImportantMetaData));
    System.out.println("Restocked vanilija: 1\n");

    printStateAndEvents(streamId, store);

    store.evolve(new SoldCommand("vanilija", factory, notImportantMetaData));
    System.out.println("Sold vanailija: 1\n");

    printStateAndEvents(streamId, store);
  }

  private void printStateAndEvents(String streamId, EventStore<TruckEventData> store) {
    System.out.println("Stock state: " + new StockState().fold(store.getAllEvents(streamId)));
    System.out.println("Events: ");
    store.getAllEvents(streamId).forEach(e -> System.out.println("\t" + e));
  }

  private void basicExamples() {
    System.out.println("==== basic example");

    TruckEventFactory factory = new TruckEventFactory();
    EventStore<TruckEventData> store = new EventStoreDB<>(repo, factory.getMapper());
    Map<String, String> notImportantMetaData = Map.of();


    Event<TruckEventData> truckCreated = factory.createTruck(notImportantMetaData);
    String streamId = truckCreated.streamId();
    store.append(truckCreated);
    System.out.println("Truck created with streamId: " + streamId);


    store.append(factory.flavourRestocked(streamId, "vanilija", 1, notImportantMetaData));
    System.out.println("Restocked vanilija: 1\n");

    store.append(factory.flavourSold(streamId, "vanilija", notImportantMetaData));
    System.out.println("Sold vanailija: 1\n");


    store.append(factory.flavourSold(streamId, "vanilija", notImportantMetaData));
    System.out.println("Sold vanailija: 1\n");

    store.append(factory.flavourSold(streamId, "jagoda", notImportantMetaData));
    System.out.println("Sold jagoda: 1\n");

    System.out.println("Projection for solding vanilija: " + new SoldOneFlavour("vanilija").fold(store.getAllEvents(streamId)));
    System.out.println("Projection for all solds:\n" + new SoldFlavoursReport().fold(store.getAllEvents(streamId)));

    System.out.println("Stock state;\n" + new StockState().fold(store.getAllEvents(streamId)));

    System.out.println("Store events;");
    store.getAllEvents(streamId).forEach(e -> System.out.println("\t" + e));
  }

}
