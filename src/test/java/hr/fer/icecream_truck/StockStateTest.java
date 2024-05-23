package hr.fer.icecream_truck;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import hr.fer.eventstore.base.EventMapper;
import hr.fer.eventstore.base.EventMapper.ClassTriple;
import hr.fer.eventstore.base.EventStore;
import hr.fer.eventstore.base.jpa.EventStoreJpaFixture;
import hr.fer.icecream_truck.events.FlavourRestocked;
import hr.fer.icecream_truck.events.FlavourSold;
import hr.fer.icecream_truck.events.TruckEventData;

class StockStateTest extends EventStoreJpaFixture {

  @Test
  void nothingInStock() {
    EventStore<TruckEventData> store = createStore(new EventMapper<>(List.of()));

    assertThat(new StockState().fold(store.getAllEvents())).isEqualTo(Map.of());
  }

  @Test
  void initialStock() {
    EventStore<TruckEventData> store = createStore(new EventMapper<>(List.of(
        EventMapper.classTriple("restocked", 0, FlavourRestocked.class))));

    store.append("truck-1", new FlavourRestocked("v", 2));
    store.append("truck-1", new FlavourRestocked("s", 2));
    store.append("truck-1", new FlavourRestocked("v", 1));

    assertThat(new StockState().fold(store.getAllEvents("truck-1"))).isEqualTo(
        Map.of("v", 3, "s", 2));
  }

  @Test
  void someSoldStock() {
    EventStore<TruckEventData> store = createStore(new EventMapper<>(List.of(
        EventMapper.classTriple("restocked", 0, FlavourRestocked.class),
        EventMapper.classTriple("sold", 0, FlavourSold.class))));

    store.append("truck-1", new FlavourRestocked("v", 3));
    store.append("truck-1", new FlavourRestocked("s", 2));
    store.append("truck-1", new FlavourSold("s"));

    assertThat(new StockState().fold(store.getAllEvents("truck-1"))).isEqualTo(Map.of("v", 3, "s", 1));
  }

}
