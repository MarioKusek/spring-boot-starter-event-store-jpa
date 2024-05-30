package hr.fer.icecream_truck.commands;

public sealed interface TruckCommand permits CreateTruck, RestockFlavour, SellFlavour {

}
