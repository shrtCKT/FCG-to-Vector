package ml.association;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HashTree<E> {
  static final int DEFAULT_NUM_BRANCHES = 3;

  static class Node<E> {
    List<ItemSet<E>> bucket;
    HashMap<Integer, Node<E>> children;

    public Node() {
      children = new HashMap<Integer, Node<E>>();
    }

    public void addToBucket(ItemSet<E> elem) {
      if (bucket == null) {
        bucket = new ArrayList<ItemSet<E>>();
      }
      bucket.add(elem);
    }
  }

  Node<E> root;
  /**
   * The number of branches for an internal node. Default is 3
   */
  int numBranches;

  /**
   * The size of an itemset stored in this tree. This implies that all itemsets should contain equal
   * number of items.
   */
  int itemsetSize;
  
  /**
   * Number of elements in the tree.
   */
  private int size;


  public HashTree(int itemsetSize) {
    initialize(itemsetSize, DEFAULT_NUM_BRANCHES);
  }

  public HashTree(int itemsetSize, int numBranches) {
    initialize(itemsetSize, numBranches);
  }
  
  private void initialize(int itemsetSize, int numBranches) {
    this.itemsetSize = itemsetSize;
    this.numBranches = numBranches;
  }

  public void insert(ItemSet<E> itemset) {
    root = insert(root, itemset, 0);
  }

  private Node<E> insert(Node<E> curr, ItemSet<E> itemset, int depth) {
    if (depth == itemsetSize) { // case 1: Leaf node
      // case 1.1: null curr then create node
      if (curr == null) {
        curr = new Node<E>();
      }
      // case 1.2: curr exists add to bucket
      curr.addToBucket(itemset);
      size++;
    } else { // case 2: non leaf node
      // case 1.1: null curr create node
      if (curr == null) {
        curr = new Node<E>();
      }
      // case 1.2: curr exists
      int key = computeKey(itemset, depth);
      Node<E> child = curr.children.get(key);
      child = insert(child, itemset, depth+1);
      curr.children.put(key, child);
    }
    
    return curr;
  }
  
  public int computeKey(ItemSet<E> itemset, int depth) {
    return itemset.getItemAt(depth).hashCode() % HashTree.DEFAULT_NUM_BRANCHES;
  }
  
  public static <T> HashTree<T> makeTree(List<ItemSet<T>> collection, int itemsetSize) {
    HashTree<T> tree = new HashTree<T>(itemsetSize);
    for (ItemSet<T> itemset : collection) {
      tree.insert(itemset);
    }
    return tree;
  }
  
  public int size() {
    return size;
  }

  public ItemSet<E> get(ItemSet<E> itemset) {
    return get(root, itemset, 0);
  }
  
  private ItemSet<E> get(Node<E> curr, ItemSet<E> itemset, int depth) {
    if (curr == null) {
      return null;
    }
    
    if (depth != itemsetSize) {
      int key = computeKey(itemset, depth);
      return get(curr.children.get(key), itemset, depth + 1);
    }
    
    int index = curr.bucket.indexOf(itemset);
    if (index == -1) {
      return null;
    }
    return curr.bucket.get(index);
    
  }
  
}
