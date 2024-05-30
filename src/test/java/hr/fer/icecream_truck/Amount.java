package hr.fer.icecream_truck;

public record Amount(int value) {
  public Amount {
    if(value < 0)
      throw new IllegalArgumentException("Amount can not be negative.");
  }

  public Amount plus(Amount amount) {
    return new Amount(value + amount.value);
  }

  public Amount decrease() {
    return new Amount(value - 1);
  }
}
