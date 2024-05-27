package hr.fer.icecream_truck;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import hr.fer.eventstore.base.Event;
import hr.fer.eventstore.base.EventStore;
import hr.fer.eventstore.base.jpa.EventStoreJpaFixture;
import hr.fer.icecream_truck.events.FlavourRestocked;
import hr.fer.icecream_truck.events.FlavourSold;
import hr.fer.icecream_truck.events.TruckEventData;

class StockStateTest extends EventStoreJpaFixture {

  private TruckEventFactory factory;
  private EventStore<TruckEventData> store;
  private Map<String, String> notImportantMetaData;
  private String streamId;

  @BeforeEach
  void setup() {
    factory = new TruckEventFactory();
    store = createStore(factory.getMapper());
    notImportantMetaData = Map.of();

    Event<TruckEventData> truckCreated = factory.createTruck(notImportantMetaData);
    store.append(truckCreated);
    streamId = truckCreated.streamId();
  }

  @Test
  void nothingInStock() {

    Map<String, Integer> foldResult = new StockState().fold(store.getAllEvents(streamId));

    assertThat(foldResult).isEqualTo(Map.of());
  }

  @Test
  void initialStock() {
    store.append(streamId, new FlavourRestocked("v", 2));
    store.append(streamId, new FlavourRestocked("s", 2));
    store.append(streamId, new FlavourRestocked("v", 1));

    Map<String, Integer> foldResult = new StockState().fold(store.getAllEvents(streamId));

    assertThat(foldResult).isEqualTo(Map.of("v", 3, "s", 2));
  }

  @Test
  void someSoldStock() {
    store.append(streamId, new FlavourRestocked("v", 3));
    store.append(streamId, new FlavourRestocked("s", 2));
    store.append(streamId, new FlavourSold("s"));

    Map<String, Integer> foldResult = new StockState().fold(store.getAllEvents(streamId));

    assertThat(foldResult).isEqualTo(Map.of("v", 3, "s", 1));
  }

}
