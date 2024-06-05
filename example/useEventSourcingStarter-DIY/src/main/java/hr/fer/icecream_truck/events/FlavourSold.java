package hr.fer.icecream_truck.events;

import hr.fer.icecream_truck.FlavourName;

public record FlavourSold(FlavourName flavour) implements StockChangeEvent {

}
