package hr.fer.icecream_truck.events;

public sealed interface TruckEventData permits StockChangeEvent, FlavourWentOutOfStock, FlavourWasNotInStock {

}
