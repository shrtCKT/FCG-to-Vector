package ml.cluster;

import java.util.Arrays;

import ml.lsh.SimHash;
import ml.lsh.SimHash.BitLength;

public class MinhashCluster implements LSHCluster<Integer, long[]> {
  SecondLevelHash secondHash;
  int numHashFn;
  int clusterIdBits;
  SimHash simhash = null;

  public MinhashCluster(int clusterIdBits, SecondLevelHash secondHash) {
    this(clusterIdBits, secondHash, -1);
  }

  public MinhashCluster(int clusterIdBits, SecondLevelHash secondHash, int numHashFn) {
    this.secondHash = secondHash;
    this.numHashFn = numHashFn;
    this.clusterIdBits = clusterIdBits;

    if (secondHash == SecondLevelHash.SimHash) {
      simhash = new SimHash(clusterIdBits <= 4 ? BitLength.Bit_4
          : clusterIdBits <= 8 ? BitLength.Bit_8
              : clusterIdBits <= 16 ? BitLength.Bit_16
                  : clusterIdBits <= 32 ? BitLength.Bit_32
                      : clusterIdBits <= 64 ? BitLength.Bit_64 : BitLength.Bit_4);
    }
  }

  @Override
  public Integer clusterId(long[] minhashSignature) {
    int bucketID;
    long[] vectorSubset;
    if (numHashFn > -1 && numHashFn < minhashSignature.length) { // if taking a subset of the
                                                                 // minhash signature is needed.
      vectorSubset = new long[numHashFn];
      for (int i = 0; i < vectorSubset.length; i++) {
        vectorSubset[i] = minhashSignature[i];
      }
    } else { // Using the entire minhash signature. Prefered.
      vectorSubset = minhashSignature;
    }

    if (secondHash == SecondLevelHash.JavaInBuilt) {
      bucketID = Arrays.hashCode(vectorSubset) % (1 << clusterIdBits);
    } else {
      bucketID = (int) simhash.hashReturnLongValue(vectorSubset);
    }
    return bucketID;
  }
}
