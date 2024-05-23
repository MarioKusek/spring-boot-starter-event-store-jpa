package hr.fer.icecream_truck.events;

public record TruckCreatedEvent(String streamId) implements TruckEventData {

}