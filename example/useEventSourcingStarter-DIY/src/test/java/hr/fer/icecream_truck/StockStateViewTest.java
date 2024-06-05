package hr.fer.icecream_truck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import hr.fer.event.StreamId;

class StockStateViewTest {
  @Test
  void noEvents() {
    StockStateView report = new StockStateView();

    Map<FlavourName, Amount> result = report.fold(List.of());

    assertThat(result).isEmpty();
  }

  @Test
  void oneRestockEvent() {
    TruckEventFactory factory = new TruckEventFactory();
    StreamId truckId = StreamId.withRandom("truck");

    StockStateView report = new StockStateView();

    Map<FlavourName, Amount> result = report.fold(List.of(
      factory.flavourRestocked(truckId, new FlavourName("a"), new Amount(7), Map.of())
    ));

    assertThat(result)
      .extractingFromEntries(e -> e.getKey().name(), e -> e.getValue().value())
      .containsOnly(
        tuple("a", 7)
    );
  }

  @Test
  void oneRestockAndSoldEvent() {
    TruckEventFactory factory = new TruckEventFactory();
    StreamId truckId = StreamId.withRandom("truck");

    StockStateView report = new StockStateView();

    Map<FlavourName, Amount> result = report.fold(List.of(
      factory.flavourRestocked(truckId, new FlavourName("a"), new Amount(7), Map.of()),
      factory.flavourSold(truckId, new FlavourName("a"), Map.of())
    ));

    assertThat(result)
      .extractingFromEntries(e -> e.getKey().name(), e -> e.getValue().value())
      .containsOnly(
        tuple("a", 6)
    );
  }

  @Test
  void oneRestockAndSoldEventsForDifferentFlavours() {
    TruckEventFactory factory = new TruckEventFactory();
    StreamId truckId = StreamId.withRandom("truck");

    StockStateView report = new StockStateView();

    Map<FlavourName, Amount> result = report.fold(List.of(
        factory.flavourRestocked(truckId, new FlavourName("a"), new Amount(7), Map.of()),
        factory.flavourRestocked(truckId, new FlavourName("b"), new Amount(3), Map.of()),
        factory.flavourSold(truckId, new FlavourName("a"), Map.of()),
        factory.flavourSold(truckId, new FlavourName("b"), Map.of()),
        factory.flavourSold(truckId, new FlavourName("b"), Map.of()),
        factory.flavourSold(truckId, new FlavourName("b"), Map.of())
        ));

    assertThat(result)
    .extractingFromEntries(e -> e.getKey().name(), e -> e.getValue().value())
    .containsOnly(
        tuple("a", 6),
        tuple("b", 0)
        );
  }

  @Test
  void negativeStock() {
    TruckEventFactory factory = new TruckEventFactory();
    StreamId truckId = StreamId.withRandom("truck");

    StockStateView report = new StockStateView();

    assertThrows(RuntimeException.class, () -> {
      report.fold(List.of(
          factory.flavourRestocked(truckId, new FlavourName("a"), new Amount(0), Map.of()),
          factory.flavourSold(truckId, new FlavourName("a"), Map.of())
          ));
    });
  }

}
