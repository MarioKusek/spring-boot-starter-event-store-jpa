package hr.fer.icecream_truck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import hr.fer.event.Event;
import hr.fer.event.StreamId;
import hr.fer.event.ddd.Result;
import hr.fer.icecream_truck.commands.RestockFlavour;
import hr.fer.icecream_truck.commands.SellFlavour;
import hr.fer.icecream_truck.events.TruckEventData;

class TruckTest {

  @Test
  void noEvents() {

    assertThrows(IllegalStateException.class, () -> {
      new Truck(List.of());
    });
  }

  @Test
  void initializeTruck() {
    TruckEventFactory factory = new TruckEventFactory();
    Map<String, String> notImportantMetaData = Map.of();
    Event<TruckEventData> truckCreated = factory.createTruck(notImportantMetaData);
    StreamId truckId = truckCreated.streamId();

    Truck truckAggregate = new Truck(List.of(
        truckCreated
        ));

    Map<FlavourName, Amount> state = truckAggregate.getFlavourState();
    assertThat(state).isEmpty();
    assertThat(truckAggregate.getId()).isEqualTo(truckId);
  }

  @Test
  void initializeTruckRestocked() {
    TruckEventFactory factory = new TruckEventFactory();
    Map<String, String> notImportantMetaData = Map.of();
    Event<TruckEventData> truckCreated = factory.createTruck(notImportantMetaData);
    StreamId truckId = truckCreated.streamId();

    Truck truckAggregate = new Truck(List.of(
        truckCreated,
        factory.flavourRestocked(truckId, new FlavourName("a"), new Amount(9), notImportantMetaData)
        ));

    Map<FlavourName, Amount> state = truckAggregate.getFlavourState();
    assertThat(state)
      .extractingFromEntries(e -> e.getKey().name(), e -> e.getValue().value())
      .containsOnly(
        tuple("a", 9)
        );
  }

  @Test
  void initializeTruckWithoutCreatingTruck() {
    TruckEventFactory factory = new TruckEventFactory();
    Map<String, String> notImportantMetaData = Map.of();
    StreamId truckId = StreamId.withRandom("truck");

    RuntimeException ex = assertThrows(RuntimeException.class, () -> {
      new Truck(List.of(
          factory.flavourRestocked(truckId, new FlavourName("a"), new Amount(9), notImportantMetaData)
          ));
    });

    assertThat(ex).hasMessage("First event should be creating truck.");
  }

  @Test
  void initializeTruckRestockedTwice() {
    TruckEventFactory factory = new TruckEventFactory();
    Map<String, String> notImportantMetaData = Map.of();
    Event<TruckEventData> truckCreated = factory.createTruck(notImportantMetaData);
    StreamId truckId = truckCreated.streamId();

    Truck truckAggregate = new Truck(List.of(
        truckCreated,
        factory.flavourRestocked(truckId, new FlavourName("a"), new Amount(9), notImportantMetaData),
        factory.flavourRestocked(truckId, new FlavourName("a"), new Amount(3), notImportantMetaData)
        ));

    Map<FlavourName, Amount> state = truckAggregate.getFlavourState();
    assertThat(state)
      .extractingFromEntries(e -> e.getKey().name(), e -> e.getValue().value())
      .containsOnly(
        tuple("a", 12)
    );
  }

  @Test
  void initializeRestockedAndSold() {
    TruckEventFactory factory = new TruckEventFactory();
    Map<String, String> notImportantMetaData = Map.of();
    Event<TruckEventData> truckCreated = factory.createTruck(notImportantMetaData);
    StreamId truckId = truckCreated.streamId();

    Truck truckAggregate = new Truck(List.of(
        truckCreated,
        factory.flavourRestocked(truckId, new FlavourName("a"), new Amount(9), notImportantMetaData),
        factory.flavourSold(truckId, new FlavourName("a"), notImportantMetaData)
        ));

    Map<FlavourName, Amount> state = truckAggregate.getFlavourState();
    assertThat(state)
      .extractingFromEntries(e -> e.getKey().name(), e -> e.getValue().value())
      .containsOnly(
        tuple("a", 8)
        );
  }

  @Test
  void restockCommand() {
    TruckEventFactory factory = new TruckEventFactory();
    Map<String, String> notImportantMetaData = Map.of();
    Event<TruckEventData> truckCreated = factory.createTruck(notImportantMetaData);
    StreamId truckId = truckCreated.streamId();
    Truck truckAggregate = new Truck(List.of(
        truckCreated
        ));

    Result<? extends RuntimeException, Event<TruckEventData>> result = truckAggregate.handle(new RestockFlavour(truckId, new FlavourName("a"), new Amount(3), notImportantMetaData));

    assertThat(result.isError()).isFalse();
    assertThat(result.events())
      .extracting("eventType")
      .containsExactly("flavourRestocked");
    Map<FlavourName, Amount> state = truckAggregate.getFlavourState();
    assertThat(state)
      .extractingFromEntries(e -> e.getKey().name(), e -> e.getValue().value())
      .containsOnly(
        tuple("a", 3)
        );
  }

  @Test
  void sellCommand() {
    TruckEventFactory factory = new TruckEventFactory();
    Map<String, String> notImportantMetaData = Map.of();
    Event<TruckEventData> truckCreated = factory.createTruck(notImportantMetaData);
    StreamId truckId = truckCreated.streamId();
    Truck truckAggregate = new Truck(List.of(
        truckCreated,
        factory.flavourRestocked(truckId, new FlavourName("a"), new Amount(9), notImportantMetaData)
        ));

    var result = truckAggregate.handle(new SellFlavour(truckId, new FlavourName("a"), notImportantMetaData));

    assertThat(result.isError()).isFalse();
    assertThat(result.events())
      .extracting("eventType")
      .containsExactly("flavorSold");
    Map<FlavourName, Amount> state = truckAggregate.getFlavourState();
    assertThat(state)
      .extractingFromEntries(e -> e.getKey().name(), e -> e.getValue().value())
      .containsOnly(
        tuple("a", 8)
        );
  }

  @Test
  void sellCommand_whenNothingLeftInStock() {
    TruckEventFactory factory = new TruckEventFactory();
    Map<String, String> notImportantMetaData = Map.of();
    Event<TruckEventData> truckCreated = factory.createTruck(notImportantMetaData);
    StreamId truckId = truckCreated.streamId();
    Truck truckAggregate = new Truck(List.of(
        truckCreated,
        factory.flavourRestocked(truckId, new FlavourName("a"), new Amount(1), notImportantMetaData)
        ));

    var result = truckAggregate.handle(new SellFlavour(truckId, new FlavourName("a"), notImportantMetaData));

    assertThat(result.isError()).isFalse();
    assertThat(result.events())
    .extracting("eventType")
    .containsExactly("flavorSold", "flavourWentOutOfStock");
    Map<FlavourName, Amount> state = truckAggregate.getFlavourState();
    assertThat(state)
    .extractingFromEntries(e -> e.getKey().name(), e -> e.getValue().value())
    .containsOnly(
        tuple("a", 0)
        );
  }

  @Test
  void sellCommand_whenNothingPutInStock() {
    TruckEventFactory factory = new TruckEventFactory();
    Map<String, String> notImportantMetaData = Map.of();
    Event<TruckEventData> truckCreated = factory.createTruck(notImportantMetaData);
    StreamId truckId = truckCreated.streamId();
    Truck truckAggregate = new Truck(List.of(
        truckCreated
        ));

    var result = truckAggregate.handle(new SellFlavour(truckId, new FlavourName("a"), notImportantMetaData));

    assertThat(result.isError()).isTrue();
    assertThat(result.events())
    .extracting("eventType")
    .containsExactly("flavourWasNotInStock");
    Map<FlavourName, Amount> state = truckAggregate.getFlavourState();
    assertThat(state).isEmpty();
  }

  @Test
  void sellCommand_whenEmptyStock() {
    TruckEventFactory factory = new TruckEventFactory();
    Map<String, String> notImportantMetaData = Map.of();
    Event<TruckEventData> truckCreated = factory.createTruck(notImportantMetaData);
    StreamId truckId = truckCreated.streamId();
    Truck truckAggregate = new Truck(List.of(
        truckCreated,
        factory.flavourRestocked(truckId, new FlavourName("a"), new Amount(0), notImportantMetaData)
        ));

    var result = truckAggregate.handle(new SellFlavour(truckId, new FlavourName("a"), notImportantMetaData));

    assertThat(result.isError()).isTrue();
    assertThat(result.events())
      .extracting("eventType")
      .containsExactly("flavourWasNotInStock");
    Map<FlavourName, Amount> state = truckAggregate.getFlavourState();
    assertThat(state)
      .extractingFromEntries(e -> e.getKey().name(), e -> e.getValue().value())
      .containsOnly(
        tuple("a", 0)
      );
  }



}
