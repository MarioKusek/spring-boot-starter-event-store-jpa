package hr.fer.icecream_truck;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import hr.fer.event.StreamId;

class SoldFlavourReportTest {

  @Test
  void noEvents() {
    SoldFlavourReport report = new SoldFlavourReport(new FlavourName("a"));

    int result = report.fold(List.of());

    assertThat(result).isEqualTo(0);
  }

  @Test
  void oneSoldEvent() {
    TruckEventFactory factory = new TruckEventFactory();
    StreamId truckId = StreamId.withRandom("truck");

    SoldFlavourReport report = new SoldFlavourReport(new FlavourName("a"));

    int result = report.fold(List.of(
        factory.flavourSold(truckId, new FlavourName("a"), Map.of())
        ));

    assertThat(result).isEqualTo(1);
  }

  @Test
  void nSoldEvents() {
    TruckEventFactory factory = new TruckEventFactory();
    StreamId truckId = StreamId.withRandom("truck");

    SoldFlavourReport report = new SoldFlavourReport(new FlavourName("a"));

    int result = report.fold(List.of(
        factory.flavourSold(truckId, new FlavourName("a"), Map.of()),
        factory.flavourSold(truckId, new FlavourName("a"), Map.of()),
        factory.flavourSold(truckId, new FlavourName("a"), Map.of())
        ));

    assertThat(result).isEqualTo(3);
  }


}
