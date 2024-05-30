package hr.fer.icecream_truck;

import java.util.List;
import java.util.Map;

import org.springframework.util.Assert;

import hr.fer.eventstore.base.Event;
import hr.fer.eventstore.base.EventProducer;
import hr.fer.icecream_truck.events.TruckEventData;

public class SoldCommand implements EventProducer<TruckEventData> {
  private TruckEventFactory factory;
  private FlavourName flavour;
  private Map<String, String> metaData;

  public SoldCommand(FlavourName flavour, TruckEventFactory factory, Map<String, String> metaData) {
    this.flavour = flavour;
    this.factory = factory;
    this.metaData = metaData;
  }

  @Override
  public List<Event<TruckEventData>> produce(List<Event<TruckEventData>> events) {
    Assert.notEmpty(events, "There sould be at least one event");
    Event<TruckEventData> event = events.getFirst();

    Map<FlavourName, Integer> state = new StockStateView().fold(events);
    int inStock = state.getOrDefault(flavour, 0);
    return switch(inStock) {
      case 0 -> List.of(factory.flavourWasNotInStock(event.streamId(), flavour, metaData));
      case 1 -> List.of(factory.flavourSold(event.streamId(), flavour, metaData), factory.flavourWentOutOfStock(event.streamId(), flavour, metaData));
      default -> List.of(factory.flavourSold(event.streamId(), flavour, metaData));
    };
  }

}
