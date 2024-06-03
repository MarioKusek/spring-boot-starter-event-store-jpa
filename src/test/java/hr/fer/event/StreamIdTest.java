package hr.fer.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class StreamIdTest {

  @Test
  void of_withoutPrefix() throws Exception {
    StreamId streamId = StreamId.ofValue("randomValue");

    assertThat(streamId.toValue()).isEqualTo("randomValue");
    assertThat(streamId.prefix()).isEmpty();
    assertThat(streamId.lastSegment()).isEqualTo("randomValue");
  }

  @Test
  void of_withPrefix() throws Exception {
    StreamId streamId = StreamId.ofValue("somePrefix-randomValue");

    assertThat(streamId.lastSegment()).isEqualTo("randomValue");
    assertThat(streamId.prefix()).isEqualTo("somePrefix");
    assertThat(streamId.toValue()).isEqualTo("somePrefix-randomValue");
  }

  @Test
  void of_withPrefixThatHasHyphen() throws Exception {
    StreamId streamId = StreamId.ofValue("some-prefix-randomValue");

    assertThat(streamId.lastSegment()).isEqualTo("randomValue");
    assertThat(streamId.prefix()).isEqualTo("some-prefix");
    assertThat(streamId.toValue()).isEqualTo("some-prefix-randomValue");
  }

  @Test
  void ofPrefix() throws Exception {
    StreamId streamId = StreamId.withRandom("somePrefix");

    assertThat(streamId.segmentSize()).isEqualTo(2);
    assertThat(streamId.prefix()).isEqualTo("somePrefix");
    assertThat(streamId.toValue()).startsWith("somePrefix-");
  }

  @Test
  void ofPrefix_withPrefixSegements() throws Exception {
    StreamId streamId = StreamId.withRandom("some", "prefix");

    assertThat(streamId.toValue()).startsWith("some-prefix-");
  }

  @Test
  void of_withMoreSegments() throws Exception {
    StreamId streamId = StreamId.ofSegments("some", "prefix", "someRandomValue");

    assertThat(streamId.toValue()).isEqualTo("some-prefix-someRandomValue");
  }

  @Test
  void of_withSegementsThatHaveHyphen() throws Exception {
    assertThrows(IllegalArgumentException.class, () -> {
      StreamId.ofSegments("some-prefix", "some-random-value");
    });
  }

  @Test
  void equalsAndHashCode() throws Exception {
    StreamId s1 = StreamId.ofValue("p-v");
    StreamId s2 = StreamId.ofSegments("p", "v");

    assertThat(s1.equals(s2)).isTrue();
    assertThat(s1.hashCode()).isEqualTo(s2.hashCode());
    assertThat(s1.equals(s1)).isTrue();
    assertThat(s1.equals("")).isFalse();
    assertThat(s1.equals(StreamId.ofValue("p1-v"))).isFalse();
    assertThat(s1.equals(StreamId.ofValue("p-v1"))).isFalse();
  }


}
