package hr.fer.icecream_truck;

import hr.fer.eventstore.base.jpa.EventStoreJpaFixture;

class SoldCommandTest extends EventStoreJpaFixture {
// TODO uncomment
//  @Test
//  void notInStock() throws Exception {
//    SoldCommand command = new SoldCommand("vanilija");
//
//    List<Event<TruckEventData>> resultEvents = command.produce(List.of());
//
//    assertThat(resultEvents)
//      .extracting("data")
//      .containsExactly(new FlavourWasNotInStock("vanilija"));
//  }

//  @Test
//  void justOneInStock() throws Exception {
//    SoldCommand command = new SoldCommand("vanilija");
//
//    List<TruckEventData> resultEvents = command.produce(List.of(new FlavourRestocked("vanilija", 1)));
//
//    assertThat(resultEvents).containsExactly(new FlavourSold("vanilija"), new FlavourWasNotInStock("vanilija"));
//  }
//
//  @Test
//  void moreInStock() throws Exception {
//    SoldCommand command = new SoldCommand("vanilija");
//
//    List<TruckEventData> resultEvents = command.produce(List.of(new FlavourRestocked("vanilija", 2)));
//
//    assertThat(resultEvents).containsExactly(new FlavourSold("vanilija"));
//  }
}
