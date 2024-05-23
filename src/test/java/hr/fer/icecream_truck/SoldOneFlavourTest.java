package hr.fer.icecream_truck;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class SoldOneFlavourTest {
  private static final Map<String, String> NOT_IMPORTANT_META_DATA = Map.of();
  TruckEventFactory factory = new TruckEventFactory();
  SoldOneFlavour p = new SoldOneFlavour("v");

  @Test
  void notSold() throws Exception {
    Integer result = p.fold(List.of());

    assertThat(result).isEqualTo(0);
  }

  @Test
  void soldOne() throws Exception {
    Integer result = p.fold(List.of(
        factory.flavourSold("NOT IMPORTANT STREAM ID", "v", NOT_IMPORTANT_META_DATA)));

    assertThat(result).isEqualTo(1);
  }

  @Test
  void soldDifferentFlawour() throws Exception {
    Integer result = p.fold(List.of(
        factory.flavourSold("NOT IMPORTANT STREAM ID", "s", NOT_IMPORTANT_META_DATA)));

    assertThat(result).isEqualTo(0);
  }

  @Test
  void sold2() throws Exception {
    Integer result = p.fold(List.of(
        factory.flavourSold("NOT IMPORTANT STREAM ID", "v", NOT_IMPORTANT_META_DATA),
        factory.flavourSold("NOT IMPORTANT STREAM ID", "v", NOT_IMPORTANT_META_DATA)
    ));

    assertThat(result).isEqualTo(2);
  }

  @Test
  void sold2Different() throws Exception {
    Integer result = p.fold(List.of(
        factory.flavourSold("NOT IMPORTANT STREAM ID", "v", NOT_IMPORTANT_META_DATA),
        factory.flavourSold("NOT IMPORTANT STREAM ID", "s", NOT_IMPORTANT_META_DATA)
    ));

    assertThat(result).isEqualTo(1);
  }

  @Test
  void soldMore() throws Exception {
    Integer result = p.fold(List.of(
        factory.flavourSold("NOT IMPORTANT STREAM ID", "v", NOT_IMPORTANT_META_DATA),
        factory.flavourSold("NOT IMPORTANT STREAM ID", "v", NOT_IMPORTANT_META_DATA),
        factory.flavourSold("NOT IMPORTANT STREAM ID", "v", NOT_IMPORTANT_META_DATA),
        factory.flavourSold("NOT IMPORTANT STREAM ID", "v", NOT_IMPORTANT_META_DATA)
        ));

    assertThat(result).isEqualTo(4);
  }
}
