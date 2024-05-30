package hr.fer.icecream_truck.commands;

import java.util.Map;

public record CreateTruck(Map<String, String> metaData) implements TruckCommand {
}