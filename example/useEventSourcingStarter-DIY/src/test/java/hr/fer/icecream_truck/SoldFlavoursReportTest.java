package hr.fer.icecream_truck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import hr.fer.event.StreamId;

class SoldFlavoursReportTest {

  @Test
  void noEvents() {
    SoldFlavoursReport report = new SoldFlavoursReport();

    Map<FlavourName, Integer> result = report.fold(List.of());

    assertThat(result).isEmpty();
  }

  @Test
  void oneSoldEvent() {
    TruckEventFactory factory = new TruckEventFactory();
    StreamId truckId = StreamId.withRandom("truck");

    SoldFlavoursReport report = new SoldFlavoursReport();

    Map<FlavourName, Integer> result = report.fold(List.of(
        factory.flavourSold(truckId, new FlavourName("a"), Map.of())
        ));

    assertThat(result)
      .extractingFromEntries(e -> e.getKey().name(), e -> e.getValue())
      .containsOnly(
        tuple("a", 1)
    );
  }

  @Test
  void soldsOneFlavourFromDifferentTrucks() {
    TruckEventFactory factory = new TruckEventFactory();
    StreamId truckId1 = StreamId.withRandom("truck");
    StreamId truckId2 = StreamId.withRandom("truck");

    SoldFlavoursReport report = new SoldFlavoursReport();

    Map<FlavourName, Integer> result = report.fold(List.of(
        factory.flavourSold(truckId1, new FlavourName("a"), Map.of()),
        factory.flavourSold(truckId2, new FlavourName("a"), Map.of()),
        factory.flavourSold(truckId2, new FlavourName("a"), Map.of())
        ));

    assertThat(result)
    .extractingFromEntries(e -> e.getKey().name(), e -> e.getValue())
    .containsOnly(
        tuple("a", 3)
        );
  }

  @Test
  void soldsDifferentFlavoursFromDifferentTrucks() {
    TruckEventFactory factory = new TruckEventFactory();
    StreamId truckId1 = StreamId.withRandom("truck");
    StreamId truckId2 = StreamId.withRandom("truck");

    SoldFlavoursReport report = new SoldFlavoursReport();

    Map<FlavourName, Integer> result = report.fold(List.of(
        factory.flavourSold(truckId1, new FlavourName("a"), Map.of()),
        factory.flavourSold(truckId2, new FlavourName("a"), Map.of()),
        factory.flavourSold(truckId2, new FlavourName("b"), Map.of())
        ));

    assertThat(result)
    .extractingFromEntries(e -> e.getKey().name(), e -> e.getValue())
    .containsOnly(
        tuple("a", 2),
        tuple("b", 1)
    );
  }


}
