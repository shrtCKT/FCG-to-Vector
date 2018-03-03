package util;

import static org.junit.Assert.*;

import java.util.HashSet;

import org.junit.Test;

public class UnorderedPairTest {

  @Test
  public void testHashCode() {
    UnorderedPair<Integer> op1 = new UnorderedPair<Integer>(4, 2);
    UnorderedPair<Integer> op2 = new UnorderedPair<Integer>(2, 4);
    UnorderedPair<Integer> op3 = new UnorderedPair<Integer>(5, 2);
    UnorderedPair<Integer> op4 = new UnorderedPair<Integer>(3, 3);
    UnorderedPair<Integer> op5 = new UnorderedPair<Integer>(5, 2);
    
    HashSet<UnorderedPair<Integer>> set = new HashSet<UnorderedPair<Integer>>();
    set.add(op1);
    set.add(op2);
    set.add(op3);
    set.add(op4);
    set.add(op5);
    
    assertEquals(3, set.size());
    assertTrue(set.contains(op1));
    assertTrue(set.contains(op3));
    assertTrue(set.contains(op4));
  }

  @Test
  public void testEqualsObject() {
    UnorderedPair<Integer> op1 = new UnorderedPair<Integer>(4, 2);
    UnorderedPair<Integer> op1Copy = new UnorderedPair<Integer>(4, 2);
    UnorderedPair<Integer> op1Reverse = new UnorderedPair<Integer>(2, 4);
    UnorderedPair<Integer> op3 = new UnorderedPair<Integer>(3, 3);
    
    assertEquals(op1, op1Copy);
    assertEquals(op1, op1Reverse);
    
    assertNotEquals(op1, op3);
  }

}
