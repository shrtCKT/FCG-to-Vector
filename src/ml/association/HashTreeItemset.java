package ml.association;

import java.util.Iterator;
import java.util.TreeSet;

/***
 * An Itemset is an unordered set of items.
 * 
 * @author meha
 *
 */
public class HashTreeItemset implements ItemSet<String>, Comparable<HashTreeItemset> {
  /**
   * We are representing it as a Tree set because we want the items to be stored in sorted order.
   */
  TreeSet<String> set;
  int supportCount;
  int hashCodeValue;

  public HashTreeItemset() {
    set = new TreeSet<String>();
    hashCodeValue = set.hashCode();
  }

  public HashTreeItemset(String[] items) {
    this();
    for (String s : items) {
      this.addItem(s);
    }
    hashCodeValue = set.hashCode();
  }

  HashTreeItemset(String[] items, int supportCount) {
    this(items);
    this.supportCount = supportCount;
  }

  @Override
  public String getItemAt(int k) {
    int i = 0;

    for (String s : set) {
      if (i == k) {
        return s;
      }
      i++;
    }

    throw new java.lang.ArrayIndexOutOfBoundsException(k);
  }

  @Override
  public void addItem(String item) {
    set.add(item);
    hashCodeValue = set.hashCode();
  }

  @Override
  public void removeItem(String item) {
    set.remove(item);
    hashCodeValue = set.hashCode();
  }

  public String[] toArray() {
    String[] s = new String[set.size()];
    set.toArray(s);
    return s;
  }

  public void incrementSupportCount() {
    this.supportCount++;
  }

  public int getSupportCount() {
    return supportCount;
  }

  public boolean equalsExcludeLastElement(HashTreeItemset other) {
    if (this.set.size() != other.set.size()) {
      return false;
    }
    if (this.set.size() == 1) {
      return true;
    }

    Iterator<String> thisIt = this.set.iterator();
    Iterator<String> otherIt = other.set.iterator();

    int i = 0;
    while (thisIt.hasNext() && otherIt.hasNext() && i++ < this.set.size() - 1) {
      String thisS = thisIt.next();
      String otherS = otherIt.next();

      int result = thisS.compareTo(otherS);
      if (result != 0) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    return hashCodeValue;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }

    HashTreeItemset other = (HashTreeItemset) obj;

    if (this == other) {
      return true;
    }

    return set.equals(other.set);
  }

  @Override
  public String toString() {
    return set.toString();
  }

  @Override
  public Iterable<String> items() {
    return set;
  }

  @Override
  public int compareTo(HashTreeItemset other) {
    Iterator<String> thisIt = this.set.iterator();
    Iterator<String> otherIt = other.set.iterator();

    while (thisIt.hasNext() && otherIt.hasNext()) {
      String thisS = thisIt.next();
      String otherS = otherIt.next();

      int result = thisS.compareTo(otherS);
      if (result != 0) {
        return result;
      }
    }

    if (thisIt.hasNext()) {
      return -1;
    }
    if (otherIt.hasNext()) {
      return 1;
    }
    return 0;
  }

  @Override
  public int size() {
    return set.size();
  }
}
