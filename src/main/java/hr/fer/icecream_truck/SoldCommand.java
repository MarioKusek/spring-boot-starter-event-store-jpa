package hr.fer.icecream_truck;

import java.util.List;

import hr.fer.eventstore.base.Event;
import hr.fer.eventstore.base.EventProducer;
import hr.fer.icecream_truck.events.TruckEventData;

public class SoldCommand implements EventProducer<TruckEventData> {

  private String flavour;

  public SoldCommand(String flavour) {
    this.flavour = flavour;
  }

  @Override
  public List<Event<TruckEventData>> produce(List<Event<TruckEventData>> events) {
    return null;
    // TODO uncomment
//    Map<String, Integer> state = new StockState().fold(events);
//    int inStock = state.getOrDefault(flavour, 0);
//    return switch(inStock) {
//      case 0 -> List.of(new FlavourWasNotInStock(flavour));
//      case 1 -> List.of(new FlavourSold(flavour), new FlavourWentOutOfStock(flavour));
//      default -> List.of(new FlavourSold(flavour));
//    };
  }

}
