package ml.lsh;

import static org.junit.Assert.*;

import java.util.BitSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SimHashTest {

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testHashEqualVector() {
    double[] fVector1 = {0.3, 0.0, 0.3, 0.4, 0.5};
    double[] fVector2 = {0.3, 0.0, 0.3, 0.4, 0.5};
    
    SimHash simhash = new SimHash(SimHash.BitLength.Bit_16);
    
    BitSet fp1 = simhash.hash(fVector1);
    BitSet fp2 = simhash.hash(fVector2);
    
    assertTrue(fp1.length() < simhash.getBitLength());
    assertTrue(fp2.length() < simhash.getBitLength());
    
    assertEquals(fp1, fp2);
  }
  
  @Test
  public void testHashCloseVector() {
    double[] fVector1 = {0.3, 0.0, 0.0, 0.4, 0.5};
    double[] fVector2 = {0.3, 0.4, 0.3, 0.5, 0.5};
    
    SimHash simhash = new SimHash(SimHash.BitLength.Bit_16);
    
    BitSet fp1 = simhash.hash(fVector1);
    BitSet fp2 = simhash.hash(fVector2);
    
    assertTrue(fp1.length() < simhash.getBitLength());
    assertTrue(fp2.length() < simhash.getBitLength());
    
    BitSet diff = new BitSet();
    diff.or(fp1);
    diff.xor(fp2);
    
    float hamming = (float) diff.cardinality() / simhash.getBitLength();
    
    assertTrue(hamming <= 0.4);
  }

}
