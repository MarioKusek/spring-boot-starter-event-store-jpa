package hr.fer.icecream_truck;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import hr.fer.eventstore.base.Event;
import hr.fer.icecream_truck.events.TruckEventData;

class SoldFlavoursReportTest {
  TruckEventFactory factory = new TruckEventFactory();
  Map<String, String> metaData = new HashMap<>();
  Event<TruckEventData> truckCreated = factory.createTruck(metaData);

  @Test
  void noSold() {
    SoldFlavoursReport p = new SoldFlavoursReport();

    Map<String, Integer> result = p.fold(List.of(truckCreated));

    assertThat(result).isEqualTo(Map.of());
  }

  @Test
  void oneSold() {
    SoldFlavoursReport p = new SoldFlavoursReport();

    Map<String, Integer> result = p.fold(List.of(
        truckCreated,
        factory.flavourSold(truckCreated.streamId(), "v", metaData)
    ));

    assertThat(result).isEqualTo(Map.of("v", 1));
  }

  @Test
  void twoSameSold() {
    SoldFlavoursReport p = new SoldFlavoursReport();

    Map<String, Integer> result = p.fold(List.of(
        truckCreated,
        factory.flavourSold(truckCreated.streamId(), "v", metaData),
        factory.flavourSold(truckCreated.streamId(), "v", metaData)
    ));

    assertThat(result).isEqualTo(Map.of("v", 2));
  }

  @Test
  void twoDifferentSold() {
    SoldFlavoursReport p = new SoldFlavoursReport();

    Map<String, Integer> result = p.fold(List.of(
        truckCreated,
        factory.flavourSold(truckCreated.streamId(), "v", metaData),
        factory.flavourSold(truckCreated.streamId(), "s", metaData)
    ));

    assertThat(result).isEqualTo(Map.of("v", 1, "s", 1));
  }

  @Test
  void manyDifferentSold() {
    SoldFlavoursReport p = new SoldFlavoursReport();

    Map<String, Integer> result = p.fold(List.of(
        truckCreated,
        factory.flavourSold(truckCreated.streamId(), "v", metaData),
        factory.flavourSold(truckCreated.streamId(), "s", metaData),
        factory.flavourSold(truckCreated.streamId(), "v", metaData),
        factory.flavourSold(truckCreated.streamId(), "v", metaData),
        factory.flavourSold(truckCreated.streamId(), "s", metaData)
        ));

    assertThat(result).isEqualTo(Map.of("v", 3, "s", 2));
  }

}
