package uk.gov.defra.reach.nipnots.entity;

import java.util.Arrays;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NipNotsNumber {
  private String prefix;
  private String leCode;
  private int index;

  public String toString() {
    return  prefix + "-30-" + leCode + "-" + luhnChecksum() + "-" + leftPad(index);
  }

  /**
   * Calculates the luhn Checksum digit
   * @return a digit between 0-9 representing the luhn checksum
   */
  public int luhnChecksum() {
    List<String> digits = Arrays.asList((leCode + "0").split(""));

    int sum = 0;
    int parity = digits.size() % 2;

    for (int i = digits.size() - 1; i >= 0; i--) {
      int digit = Integer.parseInt(digits.get(i));
      if (i % 2 == parity) {
        digit = digit * 2;
        if (digit > 9) {
          digit -= 9;
        }
      }
      sum += digit;
    }

    return (sum * 9) % 10;
  }

  /**
   * Add leading 0s(Zeros) to a string based on a desired length
   */
  public static String leftPad(int number) {
    if (number < 0) {
      throw new IndexOutOfBoundsException("number must be at least 0");
    }
    return String.format("%04d", number);
  }

  public static NipNotsNumber fromString(String s) {
    String[] parts = s.split("-");
    return new NipNotsNumber(parts[0], parts[2], Integer.parseInt(parts[4]));
  }

}
