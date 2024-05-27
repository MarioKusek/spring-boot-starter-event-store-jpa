package hr.fer.icecream_truck;

import hr.fer.eventstore.base.Event;
import hr.fer.eventstore.base.Projection;
import hr.fer.icecream_truck.events.FlavourSold;
import hr.fer.icecream_truck.events.TruckEventData;

public class SoldOneFlavour extends Projection<Integer, TruckEventData> {

  private String flavour;

  public SoldOneFlavour(String flavour) {
    this.flavour = flavour;
  }

  @Override
  public Integer initialState() {
    return 0;
  }

  @Override
  public Integer update(Integer currentState, Event<TruckEventData> event) {
    return switch (event.eventData()) {
      case FlavourSold(String sold) -> sold.equals(flavour) ? currentState + 1 : currentState;
      default -> currentState;
    };
  }

}
