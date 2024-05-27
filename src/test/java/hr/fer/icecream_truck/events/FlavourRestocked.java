package hr.fer.icecream_truck.events;

public record FlavourRestocked(String flavour, int amount) implements StockChangeEvent {

}
