package hr.fer.icecream_truck.events;

import java.util.HashMap;
import java.util.Map;

import hr.fer.eventstore.base.Event;

public class TruckEvent {
  // TODO pitanje je ovo prebaciti u EventMapper?
  private static record EventTypeAndVersion(String name, int version) { }
  private static final Map<EventTypeAndVersion, Class<? extends TruckEventData>> NAME_TO_CLASS = new HashMap<>();
  private static final Map<Class<? extends TruckEventData>, EventTypeAndVersion> CLASS_TO_NAME = new HashMap<>();

 static {
   addNameAndClass("restocked", 1, FlavourRestocked.class);
   addNameAndClass("sold", 1, FlavourSold.class);
   addNameAndClass("wentOutOfStock", 1, FlavourWentOutOfStock.class);
   addNameAndClass("wasNotInStock", 1, FlavourWasNotInStock.class);
 }

  private static void addNameAndClass(String name, int version, Class<? extends TruckEventData> clazz) {
    NAME_TO_CLASS.put(new EventTypeAndVersion(name, version), clazz);
    CLASS_TO_NAME.put(clazz, new EventTypeAndVersion(name, version));
  }

  // TODO ovo je factory za TruckEventData
  public static Event<TruckEventData> create(TruckEventData data) {
    return new Event<>("truck", null, 0, null, null);
  }
}
