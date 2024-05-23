package hr.fer.icecream_truck.events;

public sealed interface TruckEventData permits TruckCreatedEvent, StockChangeEvent, FlavourWentOutOfStock, FlavourWasNotInStock {

}
