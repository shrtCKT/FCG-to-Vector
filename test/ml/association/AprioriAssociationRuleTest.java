package ml.association;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import ml.association.AprioriAssociationRule.Rule;

import org.junit.Test;

public class AprioriAssociationRuleTest {
  private List<HashTreeItemset> initTransactions() {
    List<HashTreeItemset> transactions = new ArrayList<HashTreeItemset>(5);
    String[] trans1 = {"Bread", "Milk", "Diapers"};
    String[] trans2 = {"Bread", "Diapers", "Beer", "Eggs"};
    String[] trans3 = {"Milk", "Diapers", "Beer", "Cola"};
    String[] trans4 = {"Bread", "Milk", "Diapers", "Beer"};
    String[] trans5 = {"Bread", "Milk", "Diapers", "Cola"};
    transactions.add(new HashTreeItemset(trans1));
    transactions.add(new HashTreeItemset(trans2));
    transactions.add(new HashTreeItemset(trans3));
    transactions.add(new HashTreeItemset(trans4));
    transactions.add(new HashTreeItemset(trans5));
    
    return transactions;
  }

  private List<HashTreeItemset> initItemsetF1(boolean withSup) {
    List<HashTreeItemset> f1 = new ArrayList<HashTreeItemset>(5);
    String[] itemset1 = {"Bread"};
    String[] itemset2 = {"Beer"};
    String[] itemset3 = {"Cola"};
    String[] itemset4 = {"Milk"};
    String[] itemset5 = {"Eggs"};
    String[] itemset6 = {"Diapers"};
    if (withSup) {
      f1.add(new HashTreeItemset(itemset1, 4));
      f1.add(new HashTreeItemset(itemset2, 3));
      f1.add(new HashTreeItemset(itemset3, 2));
      f1.add(new HashTreeItemset(itemset4, 4));
      f1.add(new HashTreeItemset(itemset5, 1));
      f1.add(new HashTreeItemset(itemset6, 4));
    } else {
      f1.add(new HashTreeItemset(itemset1));
      f1.add(new HashTreeItemset(itemset2));
      f1.add(new HashTreeItemset(itemset3));
      f1.add(new HashTreeItemset(itemset4));
      f1.add(new HashTreeItemset(itemset5));
      f1.add(new HashTreeItemset(itemset6));
    }
    
    return f1;
  }

  private List<HashTreeItemset> initItemsetF2(boolean withSup) {
    List<HashTreeItemset> f2 = new ArrayList<HashTreeItemset>(5);
    String[] itemset1 = {"Beer", "Bread"};
    String[] itemset2 = {"Beer", "Diapers"};
    String[] itemset3 = {"Beer", "Milk"};
    String[] itemset4 = {"Bread", "Diapers"};
    String[] itemset5 = {"Bread", "Milk"};
    String[] itemset6 = {"Diapers", "Milk"};
    
    if (withSup) {
      f2.add(new HashTreeItemset(itemset1, 2));
      f2.add(new HashTreeItemset(itemset2, 3));
      f2.add(new HashTreeItemset(itemset3, 2));
      f2.add(new HashTreeItemset(itemset4, 4));
      f2.add(new HashTreeItemset(itemset5, 3));
      f2.add(new HashTreeItemset(itemset6, 4));
    } else {
      f2.add(new HashTreeItemset(itemset1));
      f2.add(new HashTreeItemset(itemset2));
      f2.add(new HashTreeItemset(itemset3));
      f2.add(new HashTreeItemset(itemset4));
      f2.add(new HashTreeItemset(itemset5));
      f2.add(new HashTreeItemset(itemset6));
    }
    return f2;
  }

  private List<HashTreeItemset> initItemsetF3(boolean withSup) {
    List<HashTreeItemset> f3 = new ArrayList<HashTreeItemset>(5);
    String[] itemset1 = {"Bread", "Diapers", "Milk"};
    if (withSup) {
      f3.add(new HashTreeItemset(itemset1, 3));
    } else {
      f3.add(new HashTreeItemset(itemset1));
    }
    
    return f3;
  }
  
  private List<HashTree<String>> initSupportTree(int minSup) {
    List<HashTreeItemset> F1 = initItemsetF1(true);
    List<HashTreeItemset> F2 = initItemsetF2(true);
    List<HashTreeItemset> F3 = initItemsetF3(true);
    
    // remove elements with support < minSupport
    Iterator<HashTreeItemset> it = F1.iterator();
    while (it.hasNext()) {
      HashTreeItemset i = (HashTreeItemset) it.next();
      if (i.getSupportCount() < minSup) {
        it.remove();
      }
    }
    it = F2.iterator();
    while (it.hasNext()) {
      HashTreeItemset i = (HashTreeItemset) it.next();
      if (i.getSupportCount() < minSup) {
        it.remove();
      }
    }
    it = F3.iterator();
    while (it.hasNext()) {
      HashTreeItemset i = (HashTreeItemset) it.next();
      if (i.getSupportCount() < minSup) {
        it.remove();
      }
    }
    
    // create trees
    List<HashTree<String>> supportTrees = new ArrayList<HashTree<String>>();
    supportTrees.add(HashTree.makeTree((List<ItemSet<String>>)(List<?>)F1 , 1));
    supportTrees.add(HashTree.makeTree((List<ItemSet<String>>)(List<?>)F2 , 2));
    supportTrees.add(HashTree.makeTree((List<ItemSet<String>>)(List<?>)F3 , 3));
    
    return supportTrees;
  }
  
  @Test
  public void testGenerateF1Candidates() {
    AprioriAssociationRule ap = new AprioriAssociationRule(3, 0);
    List<HashTreeItemset> transactions = initTransactions();
    List<HashTreeItemset> result = ap.generateF1Candidates(transactions);
    
    List<HashTreeItemset> expected = initItemsetF1(false);
    
    Collections.sort(result);
    Collections.sort(expected);
    
    HashTreeItemset[] result_array = new HashTreeItemset[result.size()];
    result.toArray(result_array);
    
    HashTreeItemset[] expected_array = new HashTreeItemset[expected.size()];
    expected.toArray(expected_array);
    
    assertArrayEquals(expected_array, result_array);
  }
  
  @Test
  public void testGenerateCandidatesF2() {
    AprioriAssociationRule ap = new AprioriAssociationRule(0.6F, 0);
    List<HashTreeItemset> f1 = initItemsetF1(true);
    List<HashTreeItemset> result = ap.generateCandidates(f1, 1, 5);
    
    List<HashTreeItemset> expected = initItemsetF2(false);
    
    Collections.sort(result);
    Collections.sort(expected);
    
    HashTreeItemset[] result_array = new HashTreeItemset[result.size()];
    result.toArray(result_array);
    
    HashTreeItemset[] expected_array = new HashTreeItemset[expected.size()];
    expected.toArray(expected_array);
    
    assertArrayEquals(expected_array, result_array);
  }
  
  @Test
  public void testGenerateCandidatesF3() {
    AprioriAssociationRule ap = new AprioriAssociationRule(0.6F, 0);
    List<HashTreeItemset> f2 = initItemsetF2(true);
    List<HashTreeItemset> result = ap.generateCandidates(f2, 2, 5);
    
    List<HashTreeItemset> expected = initItemsetF3(false);
    
    Collections.sort(result);
    Collections.sort(expected);
    
    HashTreeItemset[] result_array = new HashTreeItemset[result.size()];
    result.toArray(result_array);
    
    HashTreeItemset[] expected_array = new HashTreeItemset[expected.size()];
    expected.toArray(expected_array);
    
    assertArrayEquals(expected_array, result_array);
  }
  
  @Test
  public void testPrunCandidates() {
    float minSup = 0.6F;
    AprioriAssociationRule ap = new AprioriAssociationRule(minSup, 0);
    List<HashTreeItemset> f2 = initItemsetF2(true);
    
    ap.pruneCandidates(f2, 5);
    
    assertEquals(4, f2.size());
    for (HashTreeItemset i : f2) {
      assertTrue("MinSupport criteria not fulfilled!", i.getSupportCount() >= minSup);
    }
  }
  
  @Test
  public void testCountSupportF2() {
    float minSup = 0.6F;
    AprioriAssociationRule ap = new AprioriAssociationRule(minSup, 0);
    List<HashTreeItemset> transactions = initTransactions();
    List<HashTreeItemset> f2 = initItemsetF2(false);
    
    ap.countSupport(f2, transactions, 2);
    
    List<HashTreeItemset> expected = initItemsetF2(true);
    
    assertEquals(expected.size(), f2.size());
    for (int i = 0; i < expected.size(); i++) {
      assertEquals(expected.get(i), f2.get(i));
      assertEquals(expected.get(i).getSupportCount(), f2.get(i).getSupportCount());
    }
    
  }
  
  @Test
  public void testGenerateFrequentItemset() {
    float minSup = 0.6F;
    AprioriAssociationRule ap = new AprioriAssociationRule(minSup, 0);
    List<HashTreeItemset> transactions = initTransactions();
    List<HashTreeItemset> expected = initItemsetF3(false);
    
    List<HashTree<String>> allSupports = new ArrayList<HashTree<String>>();
    
    List<HashTreeItemset> result = ap.generateFrequentItemset(transactions, allSupports);
    
    HashTreeItemset[] result_array = new HashTreeItemset[result.size()];
    result.toArray(result_array);
    
    HashTreeItemset[] expected_array = new HashTreeItemset[expected.size()];
    expected.toArray(expected_array);
    
    assertArrayEquals(expected_array, result_array);
  }

  @Test
  public void testAdRuleFromConsequent() {
    AprioriAssociationRule ap = new AprioriAssociationRule(0, 0);
    
    String[] freqItemArray = {"a","b", "c", "d", "e"};
    HashTreeItemset freqItemset = new HashTreeItemset();
    for (String s : freqItemArray) {
      freqItemset.addItem(s);
    }
    
    String[] consequentArray = {"b", "e"};
    HashTreeItemset consequent = new HashTreeItemset();
    for (String s : consequentArray) {
      consequent.addItem(s);
    }
    
    String[] antesedentArray = {"a", "c", "d"};
    HashTreeItemset antesedent = new HashTreeItemset();
    for (String s : antesedentArray) {
      antesedent.addItem(s);
    }
    
    List<Rule> rules = new ArrayList<Rule>();
    
    ap.addRuleFromConsequent(freqItemset, consequent, rules);
    
    assertEquals(1, rules.size());
    
    assertEquals(antesedent, rules.get(0).getAntecedent());
    assertEquals(consequent, rules.get(0).getConsequent());
  }
  
  
  
}
