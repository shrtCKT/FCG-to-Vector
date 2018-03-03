package ml.association;

/**
 * An Itemset is an unordered set of items.
 * 
 * @author mehadi
 *
 * @param <E> the data type of an individual item in an itemset.
 */
public interface ItemSet<E> {
  /**
   * Returns the Kth item in the itemset.
   * @param k position of item.
   * @return Kth item in the itemset.
   */
  public E getItemAt(int k);
  
  public Iterable<E> items();
  
  public void addItem(E item);
  
  public void removeItem(E item);
  
  public int size();
}
