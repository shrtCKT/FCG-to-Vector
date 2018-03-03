package ml.lsh;

import java.util.BitSet;

/***
 * Implementation of simhash according to Manku, Jain, Sarma: Detecting Near-Duplicates for Web
 * Crawling. in Proceedings of the 16th international conference on World Wide Web, ACM Press, 2007
 * 
 * @author mehadi
 *
 */
public class SimHash {
  public enum BitLength {
    Bit_4, Bit_8, Bit_16, Bit_32, Bit_64
  }

  private final BitLength bitLength;

  public SimHash(BitLength bitLength) {
    this.bitLength = bitLength;
  }

  public int getBitLength() {
    return bitLength == BitLength.Bit_4 ? 4
        : bitLength == BitLength.Bit_8 ? 8
            : bitLength == BitLength.Bit_16 ? 16
                : bitLength == BitLength.Bit_32 ? 32 : bitLength == BitLength.Bit_64 ? 64 : 64;
  }

  /***
   * Computes the 4-bit hash of a double value.
   * 
   * @param value double value.
   * @return 4-bit hash.
   */
  private long doubleHash4(double value) {
    long bits = Double.doubleToLongBits(value);
    long mask = (1 << 4) - 1;
    return mask
        & (bits ^ (bits >>> 4) ^ (bits >>> 8) ^ (bits >>> 12) ^ (bits >>> 16) ^ (bits >>> 20)
            ^ (bits >>> 24) ^ (bits >>> 28) ^ (bits >>> 32) ^ (bits >>> 36) ^ (bits >>> 40)
            ^ (bits >>> 44) ^ (bits >>> 48) ^ (bits >>> 52) ^ (bits >>> 56) ^ (bits >>> 60));
  }

  /***
   * Computes the 8-bit hash of a double value.
   * 
   * @param value double value.
   * @return 8-bit hash.
   */
  private long doubleHash8(double value) {
    long bits = Double.doubleToLongBits(value);
    long mask = (1 << 8) - 1;
    return mask & (bits ^ (bits >>> 8) ^ (bits >>> 16) ^ (bits >>> 24) ^ (bits >>> 32)
        ^ (bits >>> 40) ^ (bits >>> 48) ^ (bits >>> 56));
  }

  /***
   * Computes the 16-bit hash of a double value.
   * 
   * @param value double value.
   * @return 16-bit hash.
   */
  private long doubleHash16(double value) {
    long bits = Double.doubleToLongBits(value);
    long mask = (1 << 16) - 1;
    return mask & (bits ^ (bits >>> 16) ^ (bits >>> 32) ^ (bits >>> 48));
  }

  /***
   * Computes the 32-bit hash of a double value.
   * 
   * @param value double value.
   * @return 32-bit hash.
   */
  private long doubleHash32(double value) {
    long bits = Double.doubleToLongBits(value);
    long mask = (1 << 32) - 1;
    return mask & (bits ^ (bits >>> 32));
  }

  /***
   * Computes the 64-bit hash of a double value.
   * 
   * @param value double value.
   * @return 64-bit hash.
   */
  private long doubleHash64(double value) {
    return Double.doubleToLongBits(value);
  }

  /***
   * Computes the simhash fingerprint of a given feature vector. Each feature is assumed to have
   * equal weight.
   * 
   * @param featureVector the feature vector.
   * @return simhash fingerprint.
   */
  public BitSet hash(double[] featureVector) {
    return hash(featureVector, null);
  }

  /***
   * Computes the simhash fingerprint of a given feature vector.
   * 
   * @param featureVector the feature vector.
   * @param feautureWeight the weight/importance of each feature.
   * @return simhash fingerprint.
   */
  public BitSet hash(double[] featureVector, double[] feautureWeight) {
    int[] vector = new int[getBitLength()];
    BitSet fingerPrint = new BitSet(getBitLength());

    for (int i = 0; i < featureVector.length; i++) {
      long hash;
      if (getBitLength() == 4) {
        hash = doubleHash4(featureVector[i]);
      } else if (getBitLength() == 8) {
        hash = doubleHash8(featureVector[i]);
      } else if (getBitLength() == 16) {
        hash = doubleHash16(featureVector[i]);
      } else if (getBitLength() == 32) {
        hash = doubleHash32(featureVector[i]);
      } else {
        hash = doubleHash64(featureVector[i]);
      }

      for (int j = 0; j < getBitLength(); j++) {
        long bit = hash >> j & 1;
        double weight = feautureWeight == null ? 1 : feautureWeight[i];
        if (bit == 0) {
          vector[j] -= weight;
        } else {
          vector[j] += weight;
        }
      }
    }

    for (int j = 0; j < getBitLength(); j++) {
      if (vector[j] >= 0) {
        fingerPrint.set(j);
      }
    }

    return fingerPrint;
  }
  
  /***
   * Computes the simhash fingerprint of a given feature vector. Each feature is assumed to have
   * equal weight.
   * 
   * @param featureVector the feature vector.
   * @return simhash fingerprint.
   */
  public long hashReturnLongValue(double[] featureVector) {
    return hashReturnLongValue(featureVector, null);
  }
  
  /***
   * Computes the simhash fingerprint of a given feature vector.
   * 
   * @param featureVector the feature vector.
   * @param feautureWeight the weight/importance of each feature.
   * @return simhash fingerprint.
   */
  public long hashReturnLongValue(double[] featureVector, double[] feautureWeight) {
    int[] vector = new int[getBitLength()];
    long fingerPrint = 0;

    for (int i = 0; i < featureVector.length; i++) {
      long hash;
      if (getBitLength() == 4) {
        hash = doubleHash4(featureVector[i]);
      } else if (getBitLength() == 8) {
        hash = doubleHash8(featureVector[i]);
      } else if (getBitLength() == 16) {
        hash = doubleHash16(featureVector[i]);
      } else if (getBitLength() == 32) {
        hash = doubleHash32(featureVector[i]);
      } else {
        hash = doubleHash64(featureVector[i]);
      }

      for (int j = 0; j < getBitLength(); j++) {
        long bit = hash >> j & 1;
        double weight = feautureWeight == null ? 1 : feautureWeight[i];
        if (bit == 0) {
          vector[j] -= weight;
        } else {
          vector[j] += weight;
        }
      }
    }

    for (int j = 0; j < getBitLength(); j++) {
      if (vector[j] >= 0) {
        long mask = 1 << j;
        fingerPrint = fingerPrint | mask;
      }
    }

    return fingerPrint;
  }
  
  /***
   * Computes the simhash fingerprint of a given feature vector. Each feature is assumed to have
   * equal weight.
   * 
   * @param featureVector the feature vector.
   * @return simhash fingerprint.
   */
  public long hashReturnLongValue(long[] featureVector) {
    return hashReturnLongValue(featureVector, null);
  }
  
  /***
   * Computes the simhash fingerprint of a given feature vector.
   * 
   * @param featureVector the feature vector.
   * @param feautureWeight the weight/importance of each feature.
   * @return simhash fingerprint.
   */
  public long hashReturnLongValue(long[] featureVector, double[] feautureWeight) {
    int[] vector = new int[getBitLength()];
    long fingerPrint = 0;

    for (int i = 0; i < featureVector.length; i++) {
      long hash;
      if (getBitLength() == 4) {
        hash = doubleHash4(featureVector[i]);
      } else if (getBitLength() == 8) {
        hash = doubleHash8(featureVector[i]);
      } else if (getBitLength() == 16) {
        hash = doubleHash16(featureVector[i]);
      } else if (getBitLength() == 32) {
        hash = doubleHash32(featureVector[i]);
      } else {
        hash = doubleHash64(featureVector[i]);
      }

      for (int j = 0; j < getBitLength(); j++) {
        long bit = hash >> j & 1;
        double weight = feautureWeight == null ? 1 : feautureWeight[i];
        if (bit == 0) {
          vector[j] -= weight;
        } else {
          vector[j] += weight;
        }
      }
    }

    for (int j = 0; j < getBitLength(); j++) {
      if (vector[j] >= 0) {
        long mask = 1 << j;
        fingerPrint = fingerPrint | mask;
      }
    }

    return fingerPrint;
  }
}
