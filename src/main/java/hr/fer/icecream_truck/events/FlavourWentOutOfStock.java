package hr.fer.icecream_truck.events;

public record FlavourWentOutOfStock(String flavour) implements TruckEventData {

}
