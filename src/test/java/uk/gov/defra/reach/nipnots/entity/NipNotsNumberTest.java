package uk.gov.defra.reach.nipnots.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NipNotsNumberTest {

  @Test
  void NipNotsNumber_shouldBeValid_for01234567890() {
    NipNotsNumber number = NipNotsNumber.builder()
        .prefix("PRE")
        .leCode("1234567890")
        .index(1)
        .build();

    assertThat(number.toString()).isEqualTo("PRE-30-1234567890-3-0001");
  }

  @Test
  void NipNotsNumber_shouldBeValid_for4320471429() {
    NipNotsNumber number = NipNotsNumber.builder()
        .prefix("PRE")
        .leCode("4320471429")
        .index(10001)
        .build();

    assertThat(number.toString()).isEqualTo("PRE-30-4320471429-9-10001");
  }

  @Test
  void luhnChecksum_shouldBeValid_for1234567890() {
    NipNotsNumber number = NipNotsNumber.builder().leCode("1234567890").build();
    assertThat(number.luhnChecksum()).isEqualTo(3);
  }

  @Test
  void luhnChecksum_shouldBeValid_for4320471429() {
    NipNotsNumber number = NipNotsNumber.builder().leCode("4320471429").build();
    assertThat(number.luhnChecksum()).isEqualTo(9);
  }

  @Test
  void luhnChecksum_shouldBeValid_for2410759122() {
    NipNotsNumber number = NipNotsNumber.builder().leCode("2410759122").build();
    assertThat(number.luhnChecksum()).isEqualTo(4);
  }

  @Test
  void luhnChecksum_shouldBeValid_for998429104() {
    NipNotsNumber number = NipNotsNumber.builder().leCode("998429104").build();
    assertThat(number.luhnChecksum()).isEqualTo(8);
  }

  @Test
  void luhnChecksum_shouldBeValid_for99842910432() {
    NipNotsNumber number = NipNotsNumber.builder().leCode("99842910432").build();
    assertThat(number.luhnChecksum()).isEqualTo(1);
  }

  @Test
  void leftPad_shouldReturn4charsLength_whenBelow_9999() {
    int index = 1;
    assertThat(NipNotsNumber.leftPad(index)).isEqualTo("0001");
  }

  @Test
  void leftPad_shouldReturn4charsLength_whenIndexIs10() {
    int index = 10;
    assertThat(NipNotsNumber.leftPad(index)).isEqualTo("0010");
  }

  @Test
  void leftPad_shouldReturn5charsLength_whenAbove_9999() {
    int index = 10000;
    assertThat(NipNotsNumber.leftPad(index)).isEqualTo("10000");
  }

}
