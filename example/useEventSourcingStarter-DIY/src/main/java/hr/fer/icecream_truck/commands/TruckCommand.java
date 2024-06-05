package hr.fer.icecream_truck.commands;

import hr.fer.event.StreamId;

public sealed interface TruckCommand permits RestockFlavour, SellFlavour {
  StreamId truckId();

}
