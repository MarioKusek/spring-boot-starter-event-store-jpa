package hr.fer.icecream_truck.commands;

import java.util.Map;

import hr.fer.event.StreamId;
import hr.fer.icecream_truck.FlavourName;

public record SellFlavour(StreamId truckId, FlavourName flavour, Map<String, String> metaData) implements TruckCommand {
}
