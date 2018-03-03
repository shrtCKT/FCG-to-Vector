package util.hash;

/***
 * An interface of a hash function.
 * @author mehadi
 *
 */
public interface HashFunction {
  /***
   * Generates a hash code.
   * 
   * @param data a buffer containing the data to be hashed.
   * @param offset starting index.
   * @param len length of the input buffer.
   * @return hash value.
   */
  public int hash(CharSequence data, int offset, int len);
}
