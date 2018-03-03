package util.hash;

/***
 * Wrapper around MurmurHash3
 * @author mehadi
 *
 */
public class MurmurHashWrapper implements HashFunction {
  int seed;
  
  public MurmurHashWrapper(int seed) {
    super();
    this.seed = seed;
  }

  /***
   * Generates a hash code.
   * @param data a buffer containing the data to be hashed.
   * @param offset starting index.
   * @param len length of the input buffer.
   * @return hash value.
   */
  @Override
  public int hash(CharSequence data, int offset, int len) {
    return MurmurHash3.murmurhash3_x86_32(data, offset, len, seed);
  }

}
