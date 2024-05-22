package hr.fer.icecream_truck;

import java.util.HashMap;
import java.util.Map;

import hr.fer.eventstore.base.Event;
import hr.fer.eventstore.base.Projection;
import hr.fer.icecream_truck.events.TruckEventData;

public class SoldFlavours extends Projection<Map<String, Integer>, TruckEventData> {

  @Override
  public Map<String, Integer> initialState() {
    return new HashMap<>();
  }

  @Override
  public Map<String, Integer> update(Map<String, Integer> currentState, Event<TruckEventData> event) {
    // TODO uncomment
    return null;
//    Map<String, Integer> newState = new HashMap<>(currentState);
//
//    if(event instanceof FlavourSold(String flavour))
//          newState.merge(flavour, 1, (oldValue, defaultValue) -> {
//            return oldValue + 1;
//          });
//
//    return newState;
  }

}
