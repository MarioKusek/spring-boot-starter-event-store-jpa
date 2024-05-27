package hr.fer.icecream_truck.events;

public record FlavourWasNotInStock(String flavour) implements TruckEventData {

}
