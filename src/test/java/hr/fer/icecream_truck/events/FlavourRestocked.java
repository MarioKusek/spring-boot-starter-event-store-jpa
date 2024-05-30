package hr.fer.icecream_truck.events;

import hr.fer.icecream_truck.Amount;
import hr.fer.icecream_truck.FlavourName;

public record FlavourRestocked(FlavourName flavour, Amount amount) implements StockChangeEvent {

}
