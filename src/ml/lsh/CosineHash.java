package ml.lsh;

import java.util.BitSet;
import java.util.Random;

import util.math.LinearAlgebra;

/***
 * Cosine lsh function using Random Hyper plane technique.
 * 
 * @author mehadi
 *
 */
public class CosineHash {
  int dimension; // length of feature vector to be hashed.
  int numHyperPlanes;
  Double[][] hyperPlanes;

  public CosineHash(int dimension, int numHyperPlanes, Random rng) {
    super();
    this.dimension = dimension;
    this.numHyperPlanes = numHyperPlanes;
    hyperPlanes = new Double[numHyperPlanes][dimension];
    for (int plane = 0; plane < numHyperPlanes; plane++) {
      hyperPlanes[plane] = new Double[dimension];
      for (int j = 0; j < dimension; j++) {
        hyperPlanes[plane][j] = rng.nextGaussian() - 0.5;
      }
    }
  }

  public int getDimension() {
    return dimension;
  }

  public int getNumHyperPlanes() {
    return numHyperPlanes;
  }

  /***
   * Computes the LSH cosine hash signature for a given vector.
   * 
   * @param featureVector the vector to calculate signatire for.
   * @return LSH cosine hash signature.
   */
  public BitSet signature(Double[] featureVector) {
    if (featureVector.length != dimension) {
      throw new IllegalArgumentException(
          "The given vector is not of the same dimension as this LSH.");
    }
    BitSet signature = new BitSet(numHyperPlanes);
    for (int plane = 0; plane < numHyperPlanes; plane++) {
      double dot = LinearAlgebra.dotProduct(featureVector, hyperPlanes[plane]);
      if (dot >= 0) {
        signature.set(plane);
      }
    }
    return signature;
  }

  /***
   * Computes the LSH cosine hash signature for a given vector.
   * 
   * @param featureVector the vector to calculate signatire for.
   * @return long array representation of LSH cosine hash signature.
   */
  public long[] signatureLong(Double[] featureVector) {
    BitSet signature = signature(featureVector);
    return signature.toLongArray();
  }

  /***
   * Computes the approximate angle between two vectors.
   * 
   * @param vector1
   * @param vector2
   * @return
   */
  public double angle(Double[] vector1, Double[] vector2) {
    if (vector1.length != vector2.length) {
      throw new IllegalArgumentException("The two vectors are not of the same length.");
    }
    if (vector1.length != dimension) {
      throw new IllegalArgumentException(
          "The two vectors are not of the same dimension as this LSH.");
    }

    BitSet sig1 = signature(vector1);
    BitSet sig2 = signature(vector2);

    double approx = angle(sig1, sig2, numHyperPlanes);
    return approx;
  }

  /***
   * Computes the approximate angle between two signatures.
   * 
   * @param sig1
   * @param sig2
   * @return
   */
  public static double angle(BitSet sig1, BitSet sig2, int numHyperPlanes) {
    BitSet tmp = new BitSet(numHyperPlanes);
    tmp.or(sig1);
    tmp.xor(sig2);
    // 1- to get the bits they are the same on.
    double approx = 1.0 - ((double) tmp.cardinality() / numHyperPlanes); 
    return approx;
  }

}
