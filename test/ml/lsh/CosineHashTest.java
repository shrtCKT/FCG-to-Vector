package ml.lsh;

import static org.junit.Assert.*;

import java.util.Random;
import java.util.BitSet;

import org.junit.Test;

public class CosineHashTest {

  @Test
  public void testSignature() {
    int dimension = 4;
    int numHyperPlanes = 256;
    CosineHash lsh = new CosineHash(dimension, numHyperPlanes, new Random());
    
    Double[] v1 = {0.4, 0.2, 0.1, 56.4};
    Double[] v2 = {98.0, 3.2, 0.1, 56.4};
    
    double cosine = util.math.LinearAlgebra.cosine(v1, v2);
    double exact = 1 - Math.acos(cosine) / Math.PI;
    
    BitSet sig1 = lsh.signature(v1);
    BitSet sig2 = lsh.signature(v2);
    
    double approx = CosineHash.angle(sig1, sig2, lsh.getNumHyperPlanes());
    System.out.println("Exact       = " + exact);
    System.out.println("Approximate = " + approx);
    System.out.println("Difference  = " + Math.abs(approx - exact));
    assertEquals(exact, approx, 0.0999);
  }

}
