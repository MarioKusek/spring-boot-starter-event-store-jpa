package hr.fer.icecream_truck;

import hr.fer.event.Event;
import hr.fer.event.Projection;
import hr.fer.icecream_truck.events.FlavourSold;
import hr.fer.icecream_truck.events.TruckEventData;

public class SoldFlavourReport extends Projection<Integer, TruckEventData> {

  private FlavourName flavour;

  public SoldFlavourReport(FlavourName flavour) {
    this.flavour = flavour;
  }

  @Override
  public Integer initialState() {
    return 0;
  }

  @Override
  public Integer update(Integer currentState, Event<TruckEventData> event) {
    return switch (event.eventData()) {
      case FlavourSold(FlavourName sold) -> sold.equals(flavour) ? currentState + 1 : currentState;
      default -> currentState;
    };
  }

}
