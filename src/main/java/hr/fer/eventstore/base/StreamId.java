package hr.fer.eventstore.base;

import java.util.Objects;

import io.hypersistence.tsid.TSID;

public interface StreamId {
  String toValue();

  static StreamId of(String value) {
    return new StreamId() {
      @Override
      public String toValue() {
        return value;
      }

      @Override
      public int hashCode() {
        return Objects.hash(value);
      }

      @Override
      public boolean equals(Object obj) {
        if (this == obj)
          return true;
        if (!(obj instanceof StreamId))
          return false;
        StreamId other = (StreamId) obj;
        return Objects.equals(toValue(), other.toValue());
      }

      @Override
      public String toString() {
        return String.format("StreamId[value: %s]", toValue());
      }
    };
  }

  static StreamId ofPrefix(String prefix) {
    return of(prefix + "-" + TSID.fast().toString());
  }
}
