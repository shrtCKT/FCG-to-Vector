package ml.lsh;

import java.util.BitSet;
import java.util.List;
import java.util.Random;

import util.hash.MurmurHashWrapper;

public class MinHash {
  public static class HashFn {
    final long a;
    final long b;
    long mask;
    
    /***
     * 
     * @param rng randome number generator used for generating the random hash functions
     * (permutations).
     * @param hashBitLen length of bits for each random hash function.
     */
    public HashFn(Random rng, int hashBitLen) {
      a = Math.abs(rng.nextLong());
      b = Math.abs(rng.nextLong());
      
      setMask(hashBitLen);
    }
    
    public HashFn(int a, int b, int hashBitLen) {
      this.a= a;
      this.b = b;
      
      setMask(hashBitLen);
    }
    
    public long hash(int value) {
      return( (a * value) + b) & mask;
    }
    
    private void setMask(int hashBitLen) {
      if (hashBitLen >= 63) {
        mask = 0x7FFFFFFFFFFFFFFFL;
      } else {
        mask = 1L << hashBitLen;
        mask--;
      }
    }
  }
  
  final MinHash.HashFn[] hFunctions;
  MurmurHashWrapper stringHasher;
  
  /***
   * 
   * @param numHashFunctions The number random permutations to be used in the form of hash function.
   * This determins the precision of the minhash signatire. The more the better.
   * @param rng Randome number generator used to generate numHashFunctions number of random hash 
   * functions.
   */
  public MinHash(int numHashFunctions, Random rng, int hashBitLen) {
    hFunctions = new MinHash.HashFn[numHashFunctions];
    for (int i = 0; i < numHashFunctions; i++) {
      hFunctions[i] = new HashFn(rng, hashBitLen);
    }
    stringHasher = new MurmurHashWrapper(Integer.getInteger("seed", (int) System.nanoTime()));
  }
  
  /***
   * 
   * @param hFunctions list of MinHash.HashFn.
   */
  public MinHash(MinHash.HashFn[] hFunctions) {
    this.hFunctions = hFunctions;
    stringHasher = new MurmurHashWrapper(Integer.getInteger("seed", (int) System.nanoTime()));
  }
  
  /***
   * Generates a minhash signature for given list of string tokens.
   * 
   * @param tokens list of string tokens for which signature is generated.
   * @return MinHash signature.
   */
  public long[] signature(List<String> tokens) {
    // use each token's hash code as its index in a set representation
    long[] sign = new long[hFunctions.length];
    for (int i = 0; i < sign.length; i++) {
      sign[i] = Long.MAX_VALUE;
    }
    
    for (String token : tokens) {
      for (int i = 0; i < sign.length; i++) {
        // In this case since the input is not a set we will hash the value to approximate a 
        // set representation. and then the hashed value is used as an index and passed to the 
        // minhash hashing functions.
        long value = hFunctions[i].hash(stringHasher.hash(token, 0, token.length()));
        sign[i] = Math.min(sign[i], value);
      }
    }
    
    return sign;
  }
  
  /***
   * Generates a minhash signature for given set.
   * 
   * @param set the set for which signature is generated.
   * @return  MinHash signature.
   */
  public long[] signature(BitSet set) {
    long[] sign = new long[hFunctions.length];
    for (int i = 0; i < sign.length; i++) {
      sign[i] = Long.MAX_VALUE;
    }
    
    for (int j = set.nextSetBit(0); j > -1; j = set.nextSetBit(j + 1)) {
      for (int i = 0; i < sign.length; i++) {
        long value = hFunctions[i].hash(j);
        sign[i] = Math.min(sign[i], value);
      }
    }
    
    return sign;
  }
  
  /***
   * Measures the similarity(Jaccard Index Approximation) between two minhash signatures.
   * @param sign1 minhash signature 1.
   * @param sign2 minhash signature 2.
   * @return similarity(Jaccard Index Approximation). Value between 0 and 1.
   */
  public static double similarity(long[] sign1, long[] sign2) {
    double count = 0;
    for (int i = 0; i < Math.min(sign1.length, sign2.length); i++) {
      if (sign1[i] == sign2[i]) {
        count++;
      }
    }
    return count / Math.max(sign1.length, sign2.length);
  }
}
