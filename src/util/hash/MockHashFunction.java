package util.hash;

import java.util.HashMap;

/***
 * A mock hash function for testing.
 * Usage:
 *  HashFunction hf = new MockHashFunction();
 *  
 *  hf.addMapping("string1", 3);
 *  hf.addMapping("string2", 1);
 *  
 * @author mehadi
 *
 */
public class MockHashFunction implements HashFunction {
  HashMap<CharSequence, Integer> lookupTable = null;
  
  public MockHashFunction() {
    lookupTable = new HashMap<CharSequence, Integer>();
  }
  
  public void addMapping(CharSequence key, Integer hashValue) {
    lookupTable.put(key, hashValue);
  }
  
  @Override
  public int hash(CharSequence data, int offset, int len) {
    return lookupTable.get(data.toString());
  }
}
