package hr.fer.icecream_truck;

import java.util.HashMap;
import java.util.List;

import hr.fer.event.Event;
import hr.fer.icecream_truck.events.FlavourRestocked;
import hr.fer.icecream_truck.events.FlavourSold;
import hr.fer.icecream_truck.events.StockChangeEvent;
import hr.fer.icecream_truck.events.TruckEventData;

public class Truck {

  private HashMap<FlavourName, Amount> state;

  public Truck(List<Event<TruckEventData>> events) {
    state = new HashMap<>();

    for(var event: events) {
      TruckEventData data = event.eventData();
      if(data instanceof StockChangeEvent sc)
        switch (sc) {
            case FlavourRestocked(FlavourName flavour, Amount amount) ->
              state.merge(flavour, amount, (oldValue, defaultValue) -> {
                return oldValue.plus(amount);
              });
            case FlavourSold(FlavourName flavour) ->
              state.merge(flavour, new Amount(1), (oldValue, defaultValue) -> {
                return oldValue.decrease();
              });
        };
    }
  }

  public Amount getFlavourState(FlavourName flavour) {
    return state.get(flavour);
  }

}
