package hr.fer.icecream_truck;

import java.util.HashMap;
import java.util.Map;

import hr.fer.eventstore.base.Event;
import hr.fer.eventstore.base.Projection;
import hr.fer.icecream_truck.events.FlavourRestocked;
import hr.fer.icecream_truck.events.FlavourSold;
import hr.fer.icecream_truck.events.StockChangeEvent;
import hr.fer.icecream_truck.events.TruckEventData;

public class StockStateView extends Projection<Map<String, Integer>, TruckEventData> {

  @Override
  public Map<String, Integer> initialState() {
    return Map.of();
  }

  @Override
  public Map<String, Integer> update(Map<String, Integer> currentState, Event<TruckEventData> event) {
    var newState = new HashMap<>(currentState);
    TruckEventData data = event.eventData();
    if(data instanceof StockChangeEvent sc)
      switch (sc) {
          case FlavourRestocked(String flavour, int amount) ->
            newState.merge(flavour, amount, (oldValue, defaultValue) -> {
              return oldValue + amount;
            });
          case FlavourSold(String flavour) ->
            newState.merge(flavour, 1, (oldValue, defaultValue) -> {
              return oldValue - 1;
            });
      };

    return newState;
  }

}
