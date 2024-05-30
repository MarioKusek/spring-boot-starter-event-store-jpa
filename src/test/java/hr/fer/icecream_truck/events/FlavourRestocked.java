package hr.fer.icecream_truck.events;

import hr.fer.icecream_truck.FlavourName;

public record FlavourRestocked(FlavourName flavour, int amount) implements StockChangeEvent {

}
