package ml.lsh;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Random;

import org.junit.Test;

public class MinHashTest {

  /***
   * FLAKY TEST.
   */
  @Test
  public void testSignatureListOfString() {
    Random rng = new Random();
    
    int numHashFunctions = 5;
    int hashBitLen = 10;
    MinHash minhash = new MinHash(numHashFunctions, rng, hashBitLen);
    
    String doc1 = "This is a very very similar document to next one.";
    String doc2 = "Could be a very similar document to previous one.";
    String doc3 = "Could be a exect similar document to previous one.";
    String doc4 = "But this one is a unique. So unique that there is no other like it.";
    
    long[] sig1 = minhash.signature(Arrays.asList(doc1.split("[\\s.]+")));
    System.out.println(Arrays.toString(sig1));
    long[] sig2 = minhash.signature(Arrays.asList(doc2.split("[\\s.]+")));
    System.out.println(Arrays.toString(sig2));
    long[] sig3 = minhash.signature(Arrays.asList(doc3.split("[\\s.]+")));
    System.out.println(Arrays.toString(sig3));
    long[] sig4 = minhash.signature(Arrays.asList(doc4.split("[\\s.]+")));
    System.out.println(Arrays.toString(sig4));
    
    assertTrue(MinHash.similarity(sig1, sig2) > 0.3);
    assertTrue(MinHash.similarity(sig2, sig3) > 0.6);
    assertTrue(MinHash.similarity(sig2, sig4) < 0.25);
  }

  /***
   * FLAKY TEST.
   */
  @Test
  public void testSignatureBitSet() {
    MinHash.HashFn[] hfn = new MinHash.HashFn[3];
    int hashBitLen = 10;
    Random rng = new Random();
    hfn[0] = new MinHash.HashFn(rng, hashBitLen); 
    hfn[1] = new MinHash.HashFn(rng, hashBitLen);
    hfn[2] = new MinHash.HashFn(rng, hashBitLen);
    
    MinHash minhash = new MinHash(hfn);
        
    BitSet s1 = new BitSet(); // [1, 0, 0, 1, 0]
    s1.set(0);
    s1.set(3);
    
    long[] sig1 = minhash.signature(s1);
    System.out.println(Arrays.toString(sig1));
    
    BitSet s2 = new BitSet(); // [0, 0, 1, 0, 0]
    s2.set(2);

    long[] sig2 = minhash.signature(s2);
    System.out.println(Arrays.toString(sig2));
    
    BitSet s3 = new BitSet(); // [0, 1, 0, 1, 1]
    s3.set(1);
    s3.set(3);
    s3.set(4);

    long[] sig3 = minhash.signature(s3);
    System.out.println(Arrays.toString(sig3));
    
    BitSet s4 = new BitSet(); // [1, 0, 1, 1, 0]
    s4.set(0);
    s4.set(2);
    s4.set(3);
    
    long[] sig4 = minhash.signature(s4);
    System.out.println(Arrays.toString(sig4));
    
    assertTrue(MinHash.similarity(sig1, sig2) < 0.1);
    assertTrue(MinHash.similarity(sig1, sig3) > 0.25);
    assertTrue(MinHash.similarity(sig1, sig4) > 0.25);
    assertTrue(MinHash.similarity(sig3, sig4) > 0.25);
  }

}
