package hr.fer.icecream_truck;

import java.util.List;
import java.util.Map;

import hr.fer.eventstore.base.Event;
import hr.fer.eventstore.base.EventMapper;
import hr.fer.eventstore.base.EventMapper.TypeVersion;
import hr.fer.icecream_truck.events.FlavourRestocked;
import hr.fer.icecream_truck.events.FlavourSold;
import hr.fer.icecream_truck.events.FlavourWasNotInStock;
import hr.fer.icecream_truck.events.FlavourWentOutOfStock;
import hr.fer.icecream_truck.events.TruckCreatedEvent;
import hr.fer.icecream_truck.events.TruckEventData;
import io.hypersistence.tsid.TSID;

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

  public Event<TruckEventData> flavourWasNotInStock(String streamId, String flavour, Map<String, String> metaData) {
    FlavourWasNotInStock eventData = new FlavourWasNotInStock(flavour);
    TypeVersion et = mapper.getEventTypeVersion(FlavourWasNotInStock.class);
    return new Event<>(streamId, et.type(), et.version(), eventData, metaData);
  }

  public Event<TruckEventData> flavourSold(String streamId, String flavour, Map<String, String> metaData) {
    FlavourSold eventData = new FlavourSold(flavour);
    TypeVersion et = mapper.getEventTypeVersion(FlavourSold.class);
    return new Event<>(streamId, et.type(), et.version(), eventData, metaData);
  }

  public Event<TruckEventData> flavourWentOutOfStock(String streamId, String flavour, Map<String, String> metaData) {
    FlavourWentOutOfStock eventData = new FlavourWentOutOfStock(flavour);
    TypeVersion et = mapper.getEventTypeVersion(FlavourWentOutOfStock.class);
    return new Event<>(streamId, et.type(), et.version(), eventData, metaData);
  }

  public Event<TruckEventData> createTruck(Map<String, String> metaData) {
    String streamId = "truck-" + TSID.fast().toString();
    TruckCreatedEvent eventData = new TruckCreatedEvent(streamId);
    TypeVersion et = mapper.getEventTypeVersion(TruckCreatedEvent.class);
    return new Event<>(streamId, et.type(), et.version(), eventData, metaData);
  }

  public Event<TruckEventData> flavourRestocked(String streamId, String flavour, int amount, Map<String, String> metaData) {
    FlavourRestocked eventData = new FlavourRestocked(flavour, amount);
    TypeVersion et = mapper.getEventTypeVersion(FlavourRestocked.class);
    return new Event<>(streamId, et.type(), et.version(), eventData, metaData);
  }

}
