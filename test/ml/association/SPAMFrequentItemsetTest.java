package ml.association;

import static org.junit.Assert.*;

import java.util.BitSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SPAMFrequentItemsetTest {

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testItemExtend() {
    SPAMFrequentItemset spam = new SPAMFrequentItemset(0.67);
    String[] itemsOne = {"a","b"};
    String[] itemsTwo = {"a","c"};
    HashTreeItemset itemsetOne = new HashTreeItemset(itemsOne);
    HashTreeItemset itemsetTwo = new HashTreeItemset(itemsTwo);
    
    Sequence seq = new Sequence();
    seq.addItemset(itemsetOne);
    seq.addItemset(itemsetTwo);
    
    Sequence itemExtended = spam.itemExtend(seq, "e");
    
    // Expected 
    String[] itemsTwoExtended = {"a","c", "e"};
    HashTreeItemset itemsetTwoExtended = new HashTreeItemset(itemsTwoExtended);
    Sequence expectedSeq = new Sequence();
    expectedSeq.addItemset(itemsetOne);
    expectedSeq.addItemset(itemsetTwoExtended);
    
    assertEquals(expectedSeq, itemExtended);
    
    // Test with empty
    Sequence seq2 = new Sequence();
    
    Sequence itemExtended2 = spam.itemExtend(seq2, "e");
    
    String[] itemsTwoExtended2 = {"e"};
    HashTreeItemset itemsetTwoExtended2 = new HashTreeItemset(itemsTwoExtended2);
    Sequence expectedSeq2 = new Sequence();
    expectedSeq2.addItemset(itemsetTwoExtended2);
    
    assertEquals(expectedSeq2, itemExtended2);
  }
  
  @Test
  public void testSStep() {
    SPAMFrequentItemset spam = new SPAMFrequentItemset(0.67);
    spam.setTransactionCount(4);
    
    //Testcase 1
    BitSet sequenceBitmap = new BitSet();
    
    BitSet itemBitmap = new BitSet();
    itemBitmap.set(0);
    itemBitmap.set(1);
    itemBitmap.set(2);
    
    BitSet expected = new BitSet();
  
    BitSet result = spam.sStep(sequenceBitmap, itemBitmap);
  
    assertEquals("Testcase 1", expected, result);
    
    //Testcase 1
    sequenceBitmap.clear();
    sequenceBitmap.set(0);
    
    itemBitmap.clear();
    itemBitmap.set(0);
    itemBitmap.set(1);
    itemBitmap.set(2);
    
    expected.clear();
    expected.set(1);
    expected.set(2);
  
    result = spam.sStep(sequenceBitmap, itemBitmap);
  
    assertEquals("Testcase 2", expected, result);
  }
  
  @Test
  public void testSStepWithSpecificGap() {
    SPAMFrequentItemset spamGapZero = new SPAMFrequentItemset(0.67, 0, Integer.MAX_VALUE, 0);
    SPAMFrequentItemset spamGapOne = new SPAMFrequentItemset(0.67, 0, Integer.MAX_VALUE, 1);
    SPAMFrequentItemset spamGapTwo = new SPAMFrequentItemset(0.67, 0, Integer.MAX_VALUE, 2);
    SPAMFrequentItemset spamGapThree = new SPAMFrequentItemset(0.67, 0, Integer.MAX_VALUE, 3);
    spamGapZero.setTransactionCount(8);
    spamGapOne.setTransactionCount(8);
    spamGapTwo.setTransactionCount(8);
    spamGapThree.setTransactionCount(8);
    
    BitSet sequenceBitmap = new BitSet();
    sequenceBitmap.set(0);
    sequenceBitmap.set(4);
    
    // Testcase 1: Zero gap
    BitSet itemBitmap = new BitSet();
    itemBitmap.set(1);
    itemBitmap.set(2);
    itemBitmap.set(4);
    
    
    BitSet expectedZeroGap = new BitSet();
    expectedZeroGap.set(1);
  
    BitSet resultZeroGap = spamGapZero.sStep(sequenceBitmap, itemBitmap);
  
    assertEquals("Testcase: Zero Gap", expectedZeroGap, resultZeroGap);
    
    //Testcase 2: One Gap
    BitSet expectedOneGap = new BitSet();
    expectedOneGap.set(1);
    expectedOneGap.set(2);
    
    BitSet resultOneGap = spamGapOne.sStep(sequenceBitmap, itemBitmap);
  
    assertEquals("Testcase: One Gap", expectedOneGap, resultOneGap);
    
    //Testcase 3: Two Gap
    BitSet expectedTwoGap = new BitSet();
    expectedTwoGap.set(1);
    expectedTwoGap.set(2);
    
    BitSet resultTwoGap = spamGapTwo.sStep(sequenceBitmap, itemBitmap);
  
    assertEquals("Testcase: Two Gap", expectedTwoGap, resultTwoGap);
    
    //Testcase 4: Two Gap
    BitSet expectedThreeGap = new BitSet();
    expectedThreeGap.set(1);
    expectedThreeGap.set(2);
    expectedThreeGap.set(4);
    
    BitSet resultThreeGap = spamGapThree.sStep(sequenceBitmap, itemBitmap);
  
    assertEquals("Testcase: Three Gap", expectedThreeGap, resultThreeGap);
  }

  @Test
  public void testIStep() {
    SPAMFrequentItemset spam = new SPAMFrequentItemset(0.67);
    spam.setTransactionCount(4);
    
    // Testcase 1
    BitSet sequenceBitmap = new BitSet();
    
    BitSet itemBitmap = new BitSet();
    
    BitSet result = spam.iStep(sequenceBitmap, itemBitmap);
    
    BitSet expected = new BitSet();
    
    assertEquals("Testcase 1", expected, result);
    
    // Testcase 2
    sequenceBitmap.clear();
    sequenceBitmap.set(1);
    sequenceBitmap.set(2);
    
    itemBitmap.clear();
    itemBitmap.set(0);
    itemBitmap.set(1);
    itemBitmap.set(2);
    
    expected.clear();
    expected.set(1);
    expected.set(2);
    
    result = spam.iStep(sequenceBitmap, itemBitmap);
    assertEquals("Testcase 2", expected, result);
  }

}
