package hr.fer.icecream_truck;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import hr.fer.eventstore.base.Event;
import hr.fer.eventstore.base.StreamId;
import hr.fer.eventstore.jpa.EventRepository;
import hr.fer.icecream_truck.commands.CreateTruck;
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
    StreamId truckId = service.createTruck(new CreateTruck(notImportantMetaData));
    System.out.println("Truck created with streamId: " + truckId);

    service.handle(new RestockFlavour(truckId, new FlavourName("vanilija"), new Amount(1), notImportantMetaData));
    System.out.println("Restocked vanilija: 1\n");

    printStateAndEvents(service.getAllEvents(truckId));

    service.handle(new SellFlavour(truckId, new FlavourName("vanilija"), notImportantMetaData));
    System.out.println("Sold vanailija: 1\n");

    printStateAndEvents(service.getAllEvents(truckId));
  }

  private void printStateAndEvents(List<Event<TruckEventData>> events) {
    System.out.println("Stock state: " + new StockStateView().fold(events));
    System.out.println("Events: ");
    events.forEach(e -> System.out.println("\t" + e));
  }

  private void basicExamples() {
    System.out.println("==== basic example");

    TruckService service = new TruckService(repo);
    Map<String, String> notImportantMetaData = Map.of();



    StreamId truckId = service.createTruck(new CreateTruck(notImportantMetaData));
    System.out.println("Truck created with truckId: " + truckId);

    service.handle(new RestockFlavour(truckId, new FlavourName("vanilija"), new Amount(1), notImportantMetaData));
    System.out.println("Restocked vanilija: 1\n");

    service.handle(new SellFlavour(truckId, new FlavourName("vanilija"), notImportantMetaData));
    System.out.println("Sold vanailija: 1\n");


    service.handle(new SellFlavour(truckId, new FlavourName("vanilija"), notImportantMetaData));
    System.out.println("Sold vanailija: 1\n");

    service.handle(new SellFlavour(truckId, new FlavourName("jagoda"), notImportantMetaData));
    System.out.println("Sold jagoda: 1\n");

    System.out.println("Projection for solding vanilija: " +  service.getReport(truckId, new SoldFlavourReport(new FlavourName("vanilija"))));
    System.out.println("Projection for all solds:\n" + service.getReport(truckId, new SoldFlavoursReport()));

    System.out.println("Stock state;\n" + service.getReport(truckId, new StockStateView()));

    System.out.println("Store events;");
    printStateAndEvents(service.getAllEvents(truckId));
  }

}
