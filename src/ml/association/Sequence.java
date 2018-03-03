package ml.association;

import java.util.ArrayList;
import java.util.List;

/***
 * A Sequence is an ordered list of Itemsets.
 * 
 * @author Mehadi
 *
 * @param <E> the data type of an individual item in an itemset.
 */
public class Sequence {
  List<HashTreeItemset> seq;
  private double support = 0.0;
  
  public Sequence() {
    seq = new ArrayList<HashTreeItemset>();
  }
  
  public Sequence(Sequence otherSeq) {
    seq = new ArrayList<HashTreeItemset>(otherSeq.size());

    seq.addAll(otherSeq.seq);
  }
  
  public void addItemset(HashTreeItemset elem) {
    seq.add(elem);
  }
  
  /***
   * Size of a sequence is defined as the number of itemsets in the sequence.
   * 
   * @return the number of itemsets in the sequence.
   */
  public int size() {
    return seq.size();
  }
  
  /***
   * Length is defined as the sum of the number of items in each itemset in the sequence.
   * 
   * @return the sum of the number of items in each itemset in the sequence.
   */
  public int length() {
    int len = 0;
    for(HashTreeItemset elem : seq) {
      len += elem.size();
    }
    return len;
  }
  
  /***
   * Returns the itemset at the specified index. If index is out of bounds, 
   * then throws IndexOutOfBoundsException.
   * 
   * @param index index of the sequence itemset to return.
   * @return the itemset at the specified index. 
   */
  public HashTreeItemset getElementAt(int index) {
    if (index >= seq.size()) {
      throw new IndexOutOfBoundsException(
          "Index " + index + " greater than sequence size " + seq.size());
    }
    
    return seq.get(index);
  }
  
  public Iterable<HashTreeItemset> elements() {
    return seq;
  }

  @Override
  public int hashCode() {
    return seq.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }

    Sequence other = (Sequence) obj;

    if (this == other) {
      return true;
    }

    return seq.equals(other.seq);

  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    for (HashTreeItemset elem : seq) {
      HashTreeItemset itmSet = (HashTreeItemset) elem;
      for (String item : itmSet.items()) {
        sb.append(item);
        sb.append("-");
      }
      sb.append("_");
    }

    return sb.toString();
  }

  public double getSupport() {
    return this.support;
  }
  
  public void setSupport(double support) {
    this.support = support;
  }

}
