package hr.fer.icecream_truck;

import java.util.HashMap;
import java.util.Map;

import hr.fer.eventstore.base.Event;
import hr.fer.eventstore.base.Projection;
import hr.fer.icecream_truck.events.FlavourRestocked;
import hr.fer.icecream_truck.events.FlavourSold;
import hr.fer.icecream_truck.events.StockChangeEvent;
import hr.fer.icecream_truck.events.TruckEventData;

public class StockStateView extends Projection<Map<FlavourName, Amount>, TruckEventData> {

  @Override
  public Map<FlavourName, Amount> initialState() {
    return Map.of();
  }

  @Override
  public Map<FlavourName, Amount> update(Map<FlavourName, Amount> currentState, Event<TruckEventData> event) {
    var newState = new HashMap<>(currentState);
    TruckEventData data = event.eventData();
    if(data instanceof StockChangeEvent sc)
      switch (sc) {
          case FlavourRestocked(FlavourName flavour, Amount amount) ->
            newState.merge(flavour, amount, (oldValue, defaultValue) -> {
              return oldValue.plus(amount);
            });
          case FlavourSold(FlavourName flavour) ->
            newState.merge(flavour, new Amount(1), (oldValue, defaultValue) -> {
              return oldValue.decrease();
            });
      };

    return newState;
  }

}
