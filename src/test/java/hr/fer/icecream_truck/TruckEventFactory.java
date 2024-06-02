package hr.fer.icecream_truck;

import java.util.List;
import java.util.Map;

import hr.fer.event.Event;
import hr.fer.event.StreamId;
import hr.fer.event.store.EventMapper;
import hr.fer.event.store.EventMapper.TypeVersion;
import hr.fer.icecream_truck.events.FlavourRestocked;
import hr.fer.icecream_truck.events.FlavourSold;
import hr.fer.icecream_truck.events.FlavourWasNotInStock;
import hr.fer.icecream_truck.events.FlavourWentOutOfStock;
import hr.fer.icecream_truck.events.TruckCreatedEvent;
import hr.fer.icecream_truck.events.TruckEventData;

public class TruckEventFactory {
  private EventMapper<TruckEventData> mapper;

  public TruckEventFactory() {
    this.mapper = new EventMapper<>(List.of(
      EventMapper.classTriple("truckCreated", 1, TruckCreatedEvent.class),
      EventMapper.classTriple("flavourWasNotInStock", 1, FlavourWasNotInStock.class),
      EventMapper.classTriple("flavorSold", 1, FlavourSold.class),
      EventMapper.classTriple("flavourWentOutOfStock", 1, FlavourWentOutOfStock.class),
      EventMapper.classTriple("flavourRestocked", 1, FlavourRestocked.class)
    ));
  }

  public EventMapper<TruckEventData> getMapper() {
    return mapper;
  }

  public Event<TruckEventData> flavourWasNotInStock(StreamId truckId, FlavourName flavour, Map<String, String> metaData) {
    FlavourWasNotInStock eventData = new FlavourWasNotInStock(flavour);
    TypeVersion et = mapper.getEventTypeVersion(FlavourWasNotInStock.class);
    return Event.of(truckId, et.type(), et.version(), eventData, metaData);
  }

  public Event<TruckEventData> flavourSold(StreamId truckId, FlavourName flavour, Map<String, String> metaData) {
    FlavourSold eventData = new FlavourSold(flavour);
    TypeVersion et = mapper.getEventTypeVersion(FlavourSold.class);
    return Event.of(truckId, et.type(), et.version(), eventData, metaData);
  }

  public Event<TruckEventData> flavourWentOutOfStock(StreamId truckId, FlavourName flavour, Map<String, String> metaData) {
    FlavourWentOutOfStock eventData = new FlavourWentOutOfStock(flavour);
    TypeVersion et = mapper.getEventTypeVersion(FlavourWentOutOfStock.class);
    return Event.of(truckId, et.type(), et.version(), eventData, metaData);
  }

  public Event<TruckEventData> createTruck(Map<String, String> metaData) {
    StreamId truckId = StreamId.ofPrefix("truck");
    TruckCreatedEvent eventData = new TruckCreatedEvent(truckId.toValue());
    TypeVersion et = mapper.getEventTypeVersion(TruckCreatedEvent.class);
    return Event.of(truckId, et.type(), et.version(), eventData, metaData);
  }

  public Event<TruckEventData> flavourRestocked(StreamId truckId, FlavourName flavourName, Amount amount, Map<String, String> metaData) {
    FlavourRestocked eventData = new FlavourRestocked(flavourName, amount);
    TypeVersion et = mapper.getEventTypeVersion(FlavourRestocked.class);
    return Event.of(truckId, et.type(), et.version(), eventData, metaData);
  }

}
