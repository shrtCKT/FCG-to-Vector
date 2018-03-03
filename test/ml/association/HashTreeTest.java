package ml.association;

import static org.junit.Assert.*;

import org.junit.Test;

public class HashTreeTest {

  @Test
  public void testInsert() {
    // insert one element 
    int itemsetSize = 3;
    int treeBranches = 3;
    HashTree<String> tree = new HashTree<String>(itemsetSize, treeBranches);
    
    assertEquals(0, tree.size());
    
    HashTreeItemset itemset = new HashTreeItemset();
    String item1 = "bread";
    String item2 = "butter";
    String item3 = "milk";
    itemset.addItem(item1);
    itemset.addItem(item2);
    itemset.addItem(item3);
    
    tree.insert(itemset);
    
    // check size
    assertEquals(1, tree.size());
    
    // traverse and check location
    HashTree.Node<String> curr = tree.root;
    int depth = 0;
    while (depth < itemsetSize) {
      assertNotNull(String.format("depth %d", depth), curr);
      int key = itemset.getItemAt(depth).hashCode() % treeBranches;
      curr = curr.children.get(key);
      depth++;
    }
    assertEquals(itemsetSize, depth);
    assertEquals(itemset, curr.bucket.get(0));
  }

}
