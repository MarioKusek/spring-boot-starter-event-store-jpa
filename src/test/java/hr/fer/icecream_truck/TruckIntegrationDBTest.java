package hr.fer.icecream_truck;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

import hr.fer.eventstore.base.Event;
import hr.fer.eventstore.base.EventStore;
import hr.fer.eventstore.base.jpa.EventStoreJpaFixture;
import hr.fer.icecream_truck.events.FlavourRestocked;
import hr.fer.icecream_truck.events.FlavourSold;
import hr.fer.icecream_truck.events.FlavourWentOutOfStock;
import hr.fer.icecream_truck.events.TruckCreatedEvent;
import hr.fer.icecream_truck.events.TruckEventData;

class TruckIntegrationDBTest extends EventStoreJpaFixture {

  @Test
  void basicExamples() {
    TruckEventFactory factory = new TruckEventFactory();
    EventStore<TruckEventData> store = createStore(factory.getMapper());
    Map<String, String> notImportantMetaData = Map.of();

    Event<TruckEventData> truckCreated = factory.createTruck(notImportantMetaData);
    String streamId = truckCreated.streamId();
    store.append(truckCreated);
    store.append(factory.flavourRestocked(streamId, "vanilija", 1, notImportantMetaData));
    store.append(factory.flavourSold(streamId, "vanilija", notImportantMetaData));
    store.append(factory.flavourSold(streamId, "vanilija", notImportantMetaData));
    store.append(factory.flavourSold(streamId, "jagoda", notImportantMetaData));


    assertThat(new SoldOneFlavour("vanilija").fold(store.getAllEvents(streamId))).isEqualTo(2);
    assertThat(new SoldFlavoursReport().fold(store.getAllEvents(streamId))).isEqualTo(Map.of(
        "jagoda", 1,
        "vanilija", 2));
    assertThat(new StockState().fold(store.getAllEvents(streamId))).isEqualTo(Map.of(
        "jagoda", 1,
        "vanilija", -1));
    assertThat(store.getAllEvents(streamId)).hasSize(5);
  }

  @Test
  void evolveExample() {
    TruckEventFactory factory = new TruckEventFactory();
    EventStore<TruckEventData> store = createStore(factory.getMapper());
    Map<String, String> notImportantMetaData = Map.of();

    // create truck
    Event<TruckEventData> truck = factory.createTruck(notImportantMetaData);
    store.append(truck);
    // restock vanilija 1
    store.append(factory.flavourRestocked(truck.streamId(), "vanilija", 1, notImportantMetaData));


    Map<String, Integer> fold = new StockState().fold(store.getAllEvents(truck.streamId()));
    assertThat(fold).isEqualTo(Map.of("vanilija", 1));
    assertThat(store.getAllEvents(truck.streamId()))
      .extracting("eventData")
      .containsExactly(
          new TruckCreatedEvent(truck.streamId()),
          new FlavourRestocked("vanilija", 1));

    // sold vanilija
    store.evolve(truck.streamId(), new SoldCommand("vanilija", factory, notImportantMetaData));

    fold = new StockState().fold(store.getAllEvents(truck.streamId()));
    assertThat(fold).isEqualTo(Map.of("vanilija", 0));
    assertThat(store.getAllEvents(truck.streamId()))
    .extracting("eventData")
    .containsExactly(
        new TruckCreatedEvent(truck.streamId()),
        new FlavourRestocked("vanilija", 1),
        new FlavourSold("vanilija"),
        new FlavourWentOutOfStock("vanilija"));
  }

}
