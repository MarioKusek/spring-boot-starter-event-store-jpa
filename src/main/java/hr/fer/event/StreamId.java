package hr.fer.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.hypersistence.tsid.TSID;

public final class StreamId implements Iterable<String> {
  private List<String> segments;

  private StreamId(String ...segments) {
    this(Arrays.asList(segments).stream()
        .filter(s -> !s.isEmpty())
        .toList());
  }

  private StreamId(List<String> segments) {
    for(var s: segments)
      if(s.contains("-"))
        throw new IllegalArgumentException("Segments can not have '-'.");

    this.segments = new ArrayList<>(segments);
  }

  public String toValue() {
    return segments.stream()
        .collect(Collectors.joining("-"));
  }

  @Override
  public int hashCode() {
    return Objects.hash(segments);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!(obj instanceof StreamId))
      return false;
    StreamId other = (StreamId) obj;
    return Objects.equals(segments, other.segments);
  }

  @Override
  public String toString() {
    return String.format("StreamId[value: %s]", toValue());
  }

  public static StreamId ofValue(String value) {
    String[] segmentsArray = value.split("-");

    return new StreamId(segmentsArray);
  }

  public static StreamId withRandom(String ...segments) {
    List<String> list = new LinkedList<>(Arrays.asList(segments));
    list.add(TSID.fast().toString());
    return new StreamId(list);
  }

  public static StreamId ofSegments(String ...segments) {
    return new StreamId(segments);
  }

  public String prefix() {
    return segments.stream()
        .limit(segments.size()-1)
        .collect(Collectors.joining("-"));
  }

  public String lastSegment() {
    return segments.getLast();
  }

  public int segmentSize() {
    return segments.size();
  }

  public String segment(int index) {
    return segments.get(index);
  }

  @Override
  public Iterator<String> iterator() {
    return segments.iterator();
  }

}
