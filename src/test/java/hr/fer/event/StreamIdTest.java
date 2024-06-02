package hr.fer.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import hr.fer.event.StreamId;

class StreamIdTest {

  @Test
  void of_withoutPrefix() throws Exception {
    StreamId streamId = StreamId.of("randomValue");

    assertThat(streamId.toValue()).isEqualTo("randomValue");
    assertThat(streamId.prefix()).isEmpty();
    assertThat(streamId.random()).isEqualTo("randomValue");
  }

  @Test
  void of_withPrefix() throws Exception {
    StreamId streamId = StreamId.of("somePrefix-randomValue");

    assertThat(streamId.random()).isEqualTo("randomValue");
    assertThat(streamId.prefix()).isEqualTo("somePrefix");
    assertThat(streamId.toValue()).isEqualTo("somePrefix-randomValue");
  }

  @Test
  void of_withPrefixThatHasHyphen() throws Exception {
    StreamId streamId = StreamId.of("some-prefix-randomValue");

    assertThat(streamId.random()).isEqualTo("randomValue");
    assertThat(streamId.prefix()).isEqualTo("some-prefix");
    assertThat(streamId.toValue()).isEqualTo("some-prefix-randomValue");
  }

  @Test
  void ofPrefix() throws Exception {
    StreamId streamId = StreamId.ofPrefix("somePrefix");

    assertThat(streamId.toValue()).startsWith("somePrefix-");
  }

  @Test
  void ofPrefix_withPrefixWithHyphen() throws Exception {
    StreamId streamId = StreamId.ofPrefix("some-prefix");

    assertThat(streamId.toValue()).startsWith("some-prefix-");
  }

  @Test
  void of_withTwoArguments() throws Exception {
    StreamId streamId = StreamId.of("some-prefix", "someRandomValue");

    assertThat(streamId.prefix()).isEqualTo("some-prefix");
    assertThat(streamId.random()).isEqualTo("someRandomValue");
    assertThat(streamId.toValue()).isEqualTo("some-prefix-someRandomValue");
  }

  @Test
  void of_withTwoArgumentsAndRandomValueHasHyphen() throws Exception {
    assertThrows(IllegalArgumentException.class, () -> {
      StreamId.of("some-prefix", "some-random-value");
    });
  }

  @Test
  void equalsAndHashCode() throws Exception {
    StreamId s1 = StreamId.of("p-v");
    StreamId s2 = StreamId.of("p", "v");

    assertThat(s1.equals(s2)).isTrue();
    assertThat(s1.hashCode()).isEqualTo(s2.hashCode());
    assertThat(s1.equals(s1)).isTrue();
    assertThat(s1.equals("")).isFalse();
    assertThat(s1.equals(StreamId.of("p1-v"))).isFalse();
    assertThat(s1.equals(StreamId.of("p-v1"))).isFalse();
  }


}
