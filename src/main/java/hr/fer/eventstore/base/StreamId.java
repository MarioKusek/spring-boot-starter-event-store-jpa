package hr.fer.eventstore.base;

import java.util.Objects;

import io.hypersistence.tsid.TSID;

public final class StreamId {
  private String prefix;
  private String randomValue;

  private StreamId(String prefix, String randomValue) {
    this.prefix = prefix;
    this.randomValue = randomValue;
  }

  public String prefix() {
    return prefix;

  }

  public String random() {
    return randomValue;
  }

  public String toValue() {
    if(prefix.isEmpty())
      return randomValue;
    else
      return String.format("%s-%s", prefix, randomValue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(prefix, randomValue);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!(obj instanceof StreamId))
      return false;
    StreamId other = (StreamId) obj;
    return Objects.equals(prefix, other.prefix) && Objects.equals(randomValue, other.randomValue);
  }

  @Override
  public String toString() {
    return String.format("StreamId[value: %s]", toValue());
  }

  public static StreamId of(String value) {
    int indexOfDivider = value.lastIndexOf("-");
    if (indexOfDivider == -1)
      return new StreamId("", value);
    else
      return new StreamId(value.substring(0, indexOfDivider),
          value.substring(indexOfDivider + 1, value.length()));
  }

  public static StreamId ofPrefix(String prefix) {
    return of(prefix + "-" + TSID.fast().toString());
  }

  public static StreamId of(String prefix, String randomValue) {
    if(randomValue.contains("-"))
      throw new IllegalArgumentException("Random value can not have '-'.");
    return new StreamId(prefix, randomValue);
  }


}
