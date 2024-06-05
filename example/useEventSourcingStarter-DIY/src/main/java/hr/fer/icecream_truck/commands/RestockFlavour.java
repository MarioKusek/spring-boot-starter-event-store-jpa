package hr.fer.icecream_truck.commands;

import java.util.Map;

import hr.fer.event.StreamId;
import hr.fer.icecream_truck.Amount;
import hr.fer.icecream_truck.FlavourName;

public record RestockFlavour(StreamId truckId, FlavourName flavour, Amount amount, Map<String, String> metaData) implements TruckCommand {
}