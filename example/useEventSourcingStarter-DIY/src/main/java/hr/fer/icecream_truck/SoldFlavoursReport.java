package hr.fer.icecream_truck;

import java.util.HashMap;
import java.util.Map;

import hr.fer.event.Event;
import hr.fer.event.Projection;
import hr.fer.icecream_truck.events.FlavourSold;
import hr.fer.icecream_truck.events.TruckEventData;

public class SoldFlavoursReport extends Projection<Map<FlavourName, Integer>, TruckEventData> {

  @Override
  public Map<FlavourName, Integer> initialState() {
    return new HashMap<>();
  }

  @Override
  public Map<FlavourName, Integer> update(Map<FlavourName, Integer> currentState, Event<TruckEventData> event) {
    Map<FlavourName, Integer> newState = new HashMap<>(currentState);

    if(event.eventData() instanceof FlavourSold(FlavourName flavour))
          newState.merge(flavour, 1, (oldValue, defaultValue) -> {
            return oldValue + 1;
          });

    return newState;
  }

}
