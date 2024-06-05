package hr.fer.icecream_truck.events;

public sealed interface StockChangeEvent extends TruckEventData permits FlavourRestocked, FlavourSold {

}
