package hr.fer.icecream_truck;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hr.fer.event.Event;
import hr.fer.event.StreamId;
import hr.fer.event.ddd.EventSourcesAggregate;
import hr.fer.event.ddd.Result;
import hr.fer.icecream_truck.commands.RestockFlavour;
import hr.fer.icecream_truck.commands.SellFlavour;
import hr.fer.icecream_truck.commands.TruckCommand;
import hr.fer.icecream_truck.events.FlavourRestocked;
import hr.fer.icecream_truck.events.FlavourSold;
import hr.fer.icecream_truck.events.StockChangeEvent;
import hr.fer.icecream_truck.events.TruckCreatedEvent;
import hr.fer.icecream_truck.events.TruckEventData;

public class Truck extends EventSourcesAggregate<TruckEventData, TruckCommand> {

  private static record TruckState(StreamId truckId, HashMap<FlavourName, Amount> stock) {}
  private TruckEventFactory factory;

  private TruckState state;

  public Truck(List<Event<TruckEventData>> events) {
    super(events);

    factory = new TruckEventFactory();
  }



  @Override
  protected void validateEvents(List<Event<TruckEventData>> events) {
    super.validateEvents(events);

    if(!(events.get(0).eventData() instanceof TruckCreatedEvent))
      throw new IllegalStateException("First event should be creating truck.");
  }



  @Override
  protected void evolveState(Event<TruckEventData> event) {
    TruckEventData data = event.eventData();
    if(data instanceof TruckCreatedEvent tc) {
      state = new TruckState(StreamId.ofValue(tc.streamId()), new HashMap<>());
    } else if(data instanceof StockChangeEvent sc) {
      switch (sc) {
      case FlavourRestocked(FlavourName flavour, Amount amount) ->
        state.stock().merge(flavour, amount, (oldValue, defaultValue) -> {
          return oldValue.plus(amount);
        });
      case FlavourSold(FlavourName flavour) ->
        state.stock().merge(flavour, new Amount(1), (oldValue, defaultValue) -> {
          return oldValue.decrease();
        });
      };
    }
  }

  public Amount getFlavourState(FlavourName flavour) {
    return getFlavourState().getOrDefault(flavour, new Amount(0));
  }

  public Map<FlavourName, Amount> getFlavourState() {
    HashMap<FlavourName, Amount> stock = state.stock();
    return Collections.unmodifiableMap(stock);
  }

  public StreamId getId() {
    return state.truckId();
  }

  @Override
  public Result<? extends RuntimeException, Event<TruckEventData>> handle(TruckCommand command) {
    return switch(command) {
      case RestockFlavour restockCommand -> restock(restockCommand);
      case SellFlavour sellCommand -> sell(sellCommand);
    };
  }

  private Result<? extends RuntimeException, Event<TruckEventData>> sell(SellFlavour sellCommand) {
    StreamId truckId = state.truckId();
    FlavourName flavour = sellCommand.flavour();
    Map<String, String> metaData = sellCommand.metaData();

    Result<? extends RuntimeException, Event<TruckEventData>> result = switch(getFlavourState(flavour).value()) {
      case 0 -> Result.exception(
          new IllegalStateException("Can not sell. Stock is empty."),
          factory.flavourWasNotInStock(truckId, flavour, metaData));
      case 1 -> Result.events(
          factory.flavourSold(truckId, flavour, metaData),
          factory.flavourWentOutOfStock(truckId, flavour, metaData));
      default -> Result.events(factory.flavourSold(truckId, flavour, metaData));
    };

    if(result.hasEvents())
      evolveState(result.events());

    return result;
  }

  private Result<? extends RuntimeException, Event<TruckEventData>> restock(RestockFlavour restockCommand) {
    Event<TruckEventData> restockedEvent = factory.flavourRestocked(
        state.truckId(),
        restockCommand.flavour(),
        restockCommand.amount(),
        restockCommand.metaData());
    evolveState(restockedEvent);
    return Result.events(restockedEvent);
  }
}
