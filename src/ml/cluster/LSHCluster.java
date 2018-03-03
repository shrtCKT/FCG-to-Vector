package ml.cluster;

/***
 * Locality Sensative Hashing based clustering algorithm.
 * 
 * @author mehadi
 *
 * @param <C> Cluster ID datatype.
 * @param <S> LSH Signature datatype.
 */
public interface LSHCluster<C,S> {
  public enum SecondLevelHash {
    JavaInBuilt, SimHash
  }
  
  public C clusterId(S lshSignature);
}
