package hr.fer.icecream_truck;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import hr.fer.eventstore.base.Event;
import hr.fer.icecream_truck.events.FlavourSold;
import hr.fer.icecream_truck.events.FlavourWasNotInStock;
import hr.fer.icecream_truck.events.FlavourWentOutOfStock;
import hr.fer.icecream_truck.events.TruckEventData;

class SoldCommandTest {
  private EventFactory factory;
  private Map<String, String> metaData;
  private Event<TruckEventData> truckCreated;

  @BeforeEach
  void setup() {
    factory = new EventFactory();
    metaData = Map.of("user", "x");
    truckCreated = factory.createTruck(metaData);
  }

  @Test
  void notInStock() throws Exception {
    SoldCommand command = new SoldCommand("vanilija", factory, metaData);

    List<Event<TruckEventData>> resultEvents = command.produce(List.of(truckCreated));

    assertThat(resultEvents)
      .extracting("eventData")
      .containsExactly(new FlavourWasNotInStock("vanilija"));
  }

  @Test
  void justOneInStock() throws Exception {
    SoldCommand command = new SoldCommand("vanilija", factory, metaData);

    List<Event<TruckEventData>> resultEvents = command.produce(List.of(
        truckCreated,
        factory.flavourRestocked(truckCreated.streamId(), "vanilija", 1, metaData)
    ));

    assertThat(resultEvents)
      .extracting("eventData")
      .containsExactly(new FlavourSold("vanilija"), new FlavourWentOutOfStock("vanilija"));
  }

  @Test
  void moreInStock() throws Exception {
    SoldCommand command = new SoldCommand("vanilija", factory, metaData);

    List<Event<TruckEventData>> resultEvents = command.produce(List.of(
        truckCreated,
        factory.flavourRestocked(truckCreated.streamId(), "vanilija", 2, metaData)
    ));

    assertThat(resultEvents)
      .extracting("eventData")
      .containsExactly(new FlavourSold("vanilija"));
  }
}
