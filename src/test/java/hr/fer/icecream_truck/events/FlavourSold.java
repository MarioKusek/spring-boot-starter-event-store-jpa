package hr.fer.icecream_truck.events;

public record FlavourSold(String flavour) implements StockChangeEvent {

}
