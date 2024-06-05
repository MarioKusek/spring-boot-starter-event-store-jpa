package hr.fer.icecream_truck;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import hr.fer.event.Event;
import hr.fer.event.StreamId;
import hr.fer.event.store.jpa.EventRepository;
import hr.fer.icecream_truck.commands.RestockFlavour;
import hr.fer.icecream_truck.commands.SellFlavour;
import hr.fer.icecream_truck.events.TruckEventData;
import jakarta.annotation.PostConstruct;

@Component
public class Main {
  private EventRepository repo;

  public Main(EventRepository repo) {
    this.repo = repo;
  }

  @PostConstruct
  void run() {
    basicExamples();
    evolveExample();
  }

  private void evolveExample() {
    System.out.println("==== evolve example");
    TruckService service = new TruckService(repo);
    Map<String, String> notImportantMetaData = Map.of();

    // creating truck
    StreamId truckId = service.createTruck(notImportantMetaData);
    System.out.println("Truck created with streamId: " + truckId);

    service.handle(new RestockFlavour(truckId, new FlavourName("vanilija"), new Amount(1), notImportantMetaData));
    System.out.println("Restocked vanilija: 1\n");

    printStateAndEvents(service.getAllEvents(truckId));

    service.handle(new SellFlavour(truckId, new FlavourName("vanilija"), notImportantMetaData));
    System.out.println("Sold vanailija: 1\n");

    printStateAndEvents(service.getAllEvents(truckId));
  }

  private void printStateAndEvents(List<Event<TruckEventData>> events) {
    System.out.println("Stock state: " + new StockStateView().fold(events) + "\n");
    System.out.println("Events: ");
    events.forEach(e -> System.out.println("\t" + e));
    System.out.println();
  }

  private void basicExamples() {
    System.out.println("==== basic example");

    TruckService service = new TruckService(repo);
    Map<String, String> notImportantMetaData = Map.of();



    StreamId truckId = service.createTruck(notImportantMetaData);
    System.out.println("Truck created with truckId: " + truckId);

    service.handle(new RestockFlavour(truckId, new FlavourName("vanilija"), new Amount(1), notImportantMetaData));
    System.out.println("Restocked vanilija: 1\n");

    service.handle(new SellFlavour(truckId, new FlavourName("vanilija"), notImportantMetaData));
    System.out.println("Sold vanailija: 1\n");


    try {
      System.out.println("Try to sell vanailija");
      service.handle(new SellFlavour(truckId, new FlavourName("vanilija"), notImportantMetaData));
    } catch (RuntimeException e) {
      System.out.println(e.getMessage() + "\n");
    }

    try {
      System.out.println("Try to sell jagoda");
      service.handle(new SellFlavour(truckId, new FlavourName("jagoda"), notImportantMetaData));
    } catch (RuntimeException e) {
      System.out.println(e.getMessage() + "\n");
    }

    System.out.println("Projection for solding vanilija: " +  service.getReport(truckId, new SoldFlavourReport(new FlavourName("vanilija"))) + "\n");
    System.out.println("Projection for all solds:\n" + service.getReport(truckId, new SoldFlavoursReport()) + "\n");

    System.out.println("Stock state:\n" + service.getReport(truckId, new StockStateView()) + "\n");

    System.out.println("Store report:");
    printStateAndEvents(service.getAllEvents(truckId));
  }

}
