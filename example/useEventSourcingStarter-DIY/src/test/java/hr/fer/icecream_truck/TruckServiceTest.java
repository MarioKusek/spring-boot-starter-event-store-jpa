package hr.fer.icecream_truck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import hr.fer.event.Projection;
import hr.fer.event.StreamId;
import hr.fer.event.store.EventStoreInMemory;
import hr.fer.icecream_truck.commands.RestockFlavour;
import hr.fer.icecream_truck.commands.SellFlavour;
import hr.fer.icecream_truck.events.TruckEventData;

class TruckServiceTest {
  static Projection<HashSet<StreamId>, TruckEventData> truckIdsProjection = Projection
    .create(() -> new HashSet<StreamId>(), (s, e) -> {
      if (e.eventType().equals("truckCreated")) {
        s.add(e.streamId());
      }
      return s;
    });

  @Test
  void truckNotCreated() throws Exception {
    TruckService service = new TruckService(EventStoreInMemory::new);

    Set<StreamId> truckIds = service.getReport(truckIdsProjection);

    assertThat(truckIds).hasSize(0);
  }

  @Test
  void oneTruckCreated() throws Exception {
    TruckService service = new TruckService(EventStoreInMemory::new);
    Map<String, String> notImportantMetaData = Map.of();

    StreamId createdTruckId = service.createTruck(notImportantMetaData);

    Set<StreamId> truckIds = service.getReport(truckIdsProjection);
    assertThat(truckIds).hasSize(1);
    assertThat(service.getAllEvents(createdTruckId))
      .extracting("eventType")
      .containsExactly("truckCreated");
  }

  @Test
  void restockFlavour() throws Exception {
    TruckService service = new TruckService(EventStoreInMemory::new);
    Map<String, String> notImportantMetaData = Map.of();
    StreamId truckId = service.createTruck(notImportantMetaData);

    service.handle(new RestockFlavour(truckId, new FlavourName("vanilija"), new Amount(10), notImportantMetaData));

    Map<FlavourName, Amount> stockReport = service.getReport(truckId, new StockStateView());

    assertThat(stockReport)
      .extractingFromEntries(e -> e.getKey().name(), e -> e.getValue().value())
      .containsOnly(
          tuple("vanilija", 10)
      );
    assertThat(service.getAllEvents(truckId))
      .extracting("eventType")
      .containsExactly("truckCreated", "flavourRestocked");
  }

  @Test
  void sellFlavour() throws Exception {
    TruckService service = new TruckService(EventStoreInMemory::new);
    Map<String, String> notImportantMetaData = Map.of();
    StreamId truckId = service.createTruck(notImportantMetaData);
    service.handle(new RestockFlavour(truckId, new FlavourName("vanilija"), new Amount(10), notImportantMetaData));

    service.handle(new SellFlavour(truckId, new FlavourName("vanilija"), notImportantMetaData));

    Map<FlavourName, Amount> stockReport = service.getReport(truckId, new StockStateView());

    assertThat(stockReport)
      .extractingFromEntries(e -> e.getKey().name(), e -> e.getValue().value())
      .containsOnly(
          tuple("vanilija", 9)
      );
    assertThat(service.getAllEvents(truckId))
      .extracting("eventType")
      .containsExactly("truckCreated", "flavourRestocked", "flavorSold");
  }

  @Test
  void sellFlavourWhenNotInStock() throws Exception {
    TruckService service = new TruckService(EventStoreInMemory::new);
    Map<String, String> notImportantMetaData = Map.of();
    StreamId truckId = service.createTruck(notImportantMetaData);
    service.handle(new RestockFlavour(truckId, new FlavourName("vanilija"), new Amount(0), notImportantMetaData));

    assertThrows(RuntimeException.class, () -> {
      service.handle(new SellFlavour(truckId, new FlavourName("vanilija"), notImportantMetaData));
    });

    Map<FlavourName, Amount> stockReport = service.getReport(truckId, new StockStateView());
    assertThat(stockReport)
      .extractingFromEntries(e -> e.getKey().name(), e -> e.getValue().value())
      .containsOnly(
          tuple("vanilija", 0)
          );
    assertThat(service.getAllEvents(truckId))
      .extracting("eventType")
      .containsExactly("truckCreated", "flavourRestocked", "flavourWasNotInStock");
  }

  @Test
  void sellFlavourWhenNotJetInStock() throws Exception {
    TruckService service = new TruckService(EventStoreInMemory::new);
    Map<String, String> notImportantMetaData = Map.of();
    StreamId truckId = service.createTruck(notImportantMetaData);

    assertThrows(RuntimeException.class, () -> {
      service.handle(new SellFlavour(truckId, new FlavourName("vanilija"), notImportantMetaData));
    });


    Map<FlavourName, Amount> stockReport = service.getReport(truckId, new StockStateView());
    assertThat(stockReport).isEmpty();
    assertThat(service.getAllEvents(truckId))
      .extracting("eventType")
      .containsExactly("truckCreated", "flavourWasNotInStock");
  }

  @Test
  void sellFlavourAndItWentOutOfStock() throws Exception {
    TruckService service = new TruckService(EventStoreInMemory::new);
    Map<String, String> notImportantMetaData = Map.of();
    StreamId truckId = service.createTruck(notImportantMetaData);
    service.handle(new RestockFlavour(truckId, new FlavourName("vanilija"), new Amount(1), notImportantMetaData));

    service.handle(new SellFlavour(truckId, new FlavourName("vanilija"), notImportantMetaData));

    Map<FlavourName, Amount> stockReport = service.getReport(truckId, new StockStateView());

    assertThat(stockReport)
      .extractingFromEntries(e -> e.getKey().name(), e -> e.getValue().value())
      .containsOnly(
          tuple("vanilija", 0)
      );
    assertThat(service.getAllEvents(truckId))
      .extracting("eventType")
      .containsExactly("truckCreated", "flavourRestocked", "flavorSold", "flavourWentOutOfStock");
  }
}
