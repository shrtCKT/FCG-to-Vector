package ml.cluster;

import java.util.Arrays;
import java.util.BitSet;

import ml.lsh.SimHash;
import ml.lsh.SimHash.BitLength;

public class CosineLSHCluster implements LSHCluster<Integer, BitSet> {
  SecondLevelHash secondHash;
  /***
   * The number of bits used to represent a clusterID will determin the number of clusters as
   * pow(2, clusterIdBits + 1)
   * 
   */
  int clusterIdBits;
  /***
   * The Cosine LSH Signature will be broken in to bands of size bandBits.
   */
  int bandBits;
  /***
   * The length of the cosine lsh signatiure in number of bits.
   */
  int signatureBitLength;
  SimHash simhash = null;
  
  public CosineLSHCluster(int clusterIdBits, SecondLevelHash secondHash, int numBitsSignature) {
    this(clusterIdBits, secondHash, numBitsSignature, 16);
  }
  
  public CosineLSHCluster(int clusterIdBits, SecondLevelHash secondHash, int signatureBitLength,
      int bandBits) {
    if (bandBits > 64) {
      throw new IllegalArgumentException("BandBits greater than 64. bandBits = " + bandBits);
    }
    this.secondHash = secondHash;
    this.clusterIdBits = clusterIdBits;
    this.bandBits = bandBits;
    this.signatureBitLength = signatureBitLength;
    
    if (secondHash == SecondLevelHash.SimHash) {
     simhash = new SimHash(clusterIdBits <= 4 ? BitLength.Bit_4
       : clusterIdBits <= 8 ? BitLength.Bit_8
       : clusterIdBits <= 16 ? BitLength.Bit_16
       : clusterIdBits <= 32 ? BitLength.Bit_32
       : clusterIdBits <= 64 ? BitLength.Bit_64 : BitLength.Bit_4);
    }
  }
  
  @Override
  public Integer clusterId(BitSet cosineLSHSignature) {
    int bucketID;
    long[] bands = new long[(int)Math.ceil((double)signatureBitLength / bandBits)];
    
    for (int i = 0; i < bands.length; i++) {
      BitSet bandBitSet = cosineLSHSignature.get(i * bandBits, (i+1) * bandBits);
      bands[i] = bandBitSet.isEmpty() ? 0L : bandBitSet.toLongArray()[0];
    }
    
    if (secondHash == SecondLevelHash.JavaInBuilt) {
      bucketID = Arrays.hashCode(bands) % (1 << clusterIdBits);
    } else {
      bucketID = (int) simhash.hashReturnLongValue(bands);
    }
    
    return bucketID;
  }
  
}