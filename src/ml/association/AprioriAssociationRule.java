package ml.association;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ml.association.HashTree.Node;
import ml.data.Attribute;
import ml.data.DataInstance;

public class AprioriAssociationRule {

  public static class Rule {
    HashTreeItemset antecedent;
    HashTreeItemset consequent;

    float confidence;

    public Rule() {
      antecedent = new HashTreeItemset();
      consequent = new HashTreeItemset();
    }

    /**
     * Copy constructor.
     * 
     * @param r rule to copy.
     */
    public Rule(Rule r) {
      this();
      this.confidence = r.confidence;
      for (String item : r.antecedent.items()) {
        this.antecedent.addItem(item);
      }
      for (String item : r.consequent.items()) {
        this.consequent.addItem(item);
      }
    }

    public Rule(HashTreeItemset antecedent, HashTreeItemset consequent) {
      this();

      for (String item : antecedent.items()) {
        this.antecedent.addItem(item);
      }
      for (String item : consequent.items()) {
        this.consequent.addItem(item);
      }
    }

    public float getConfidence() {
      return confidence;
    }

    public void setConfidence(float confidence) {
      this.confidence = confidence;
    }

    public HashTreeItemset getAntecedent() {
      return antecedent;
    }

    public void setAntecedent(HashTreeItemset antecedent) {
      this.antecedent = antecedent;
    }

    public HashTreeItemset getConsequent() {
      return consequent;
    }

    public void setConsequent(HashTreeItemset consequent) {
      this.consequent = consequent;
    }

    @Override
    public String toString() {
      return String.format("%s --> %s", antecedent.toString(), consequent.toString());
    }
  }

  private float minSupport;
  private float minConfidence;
  private List<Rule> rules;
  private boolean verbose;

  public AprioriAssociationRule(float minSupport, float minConfidence) {
    this(minSupport, minConfidence, false);
  }
  
  public AprioriAssociationRule(float minSupport, float minConfidence, boolean verbose) {
    super();
    this.minSupport = minSupport;
    this.minConfidence = minConfidence;
    rules = null;
    this.verbose = verbose;
  }

  public static HashTreeItemset dataInstaceToItemset(DataInstance instance, List<Attribute> attributeList) {
    HashTreeItemset itemset = new HashTreeItemset();
    
    for (int i = 0; i < attributeList.size(); i++) {
      String val = (String) instance.getAttributeValueAt(i);
      if (val.equals("1")) {
        itemset.addItem(attributeList.get(i).getName());
      }
    }
    
    return itemset; 
  }
  
  public void learn(List<HashTreeItemset> transactions) {
    List<HashTree<String>> allSupports = new ArrayList<HashTree<String>>();
    // generate frequent itemset
    List<HashTreeItemset> freqItemset = generateFrequentItemset(transactions, allSupports);
    
    // generate rules
    rules = generateRules(freqItemset, allSupports);
  }
  
  /**
   * Generates Association Rules using Apprior rule generation algorithm based 
   * on Algorithm 6.2 on page 351 in "Introduction to Data Mining" by Pang-Ning Tan.
   * @param freqItemset
   * @param allSupports
   * @return
   */
  public List<Rule> generateRules(List<HashTreeItemset> freqItemset, List<HashTree<String>> allSupports) {
    List<Rule> rules = new LinkedList<Rule>();
    for (HashTreeItemset its : freqItemset) {
      List<HashTreeItemset> candidateConsequents = new ArrayList<HashTreeItemset>();
      // first lets add all items, in itemset, to antecedent.
      for (String item : its.items()) {
        HashTreeItemset candidate = new HashTreeItemset();
        candidate.addItem(item);
        candidateConsequents.add(candidate);
      }

      generateRules(its, candidateConsequents, rules, allSupports);
    }

    return rules;
  }
  
  /**
   * Generates Association Rules using Apprior rule generation algorithm based 
   * on Algorithm 6.3 on page 352 in "Introduction to Data Mining" by Pang-Ning Tan.
   * @param freqItemset
   * @param allSupports
   * @return
   */
  private void generateRules(HashTreeItemset freqItemset,
      List<HashTreeItemset> consequents, List<Rule> rules, List<HashTree<String>> allSupports) {
    if (consequents.isEmpty()) {
      return;
    }
    
    int k = freqItemset.size();
    int m = consequents.get(0).size();
    
    if (k <= m + 1) {
      return;
    }
    
    List<HashTreeItemset> candicateCons = generateCandidateConsequent(consequents);
    
    // prune
    pruneCandidateConsequents(freqItemset, candicateCons, allSupports);
    
    // add rules based on pruned consequents
    for (HashTreeItemset consequent : candicateCons) {
      addRuleFromConsequent(freqItemset, consequent, rules);
    }
    
    // recursive call to next level
    generateRules(freqItemset, candicateCons, rules, allSupports);
  }

  private List<HashTreeItemset> generateCandidateConsequent(List<HashTreeItemset> hmConsequents) {
    List<HashTreeItemset> candidates = new LinkedList<HashTreeItemset>();
    for (int i = 0; i < hmConsequents.size(); i++) {
      for (int j = i + 1; j < hmConsequents.size(); j++) {
        // if itemset has same elements apart from last then
        if (!hmConsequents.get(i).equalsExcludeLastElement(hmConsequents.get(j))) {
          break;
        }

        // merge consequents
        HashTreeItemset itemset = new HashTreeItemset();
        for (String s : hmConsequents.get(i).items()) {
          itemset.addItem(s);
        }
        itemset.addItem(hmConsequents.get(j).getItemAt(hmConsequents.get(j).size() - 1));

        candidates.add(itemset);
      }
    }
    return candidates;
  }

  private void pruneCandidateConsequents(HashTreeItemset freqItemset, List<HashTreeItemset> candicateCons, List<HashTree<String>> allSupports) {
    Iterator<HashTreeItemset> it = candicateCons.iterator();
    while(it.hasNext()) {
      HashTreeItemset cons = it.next();
      float numeratorSupport = freqItemset.getSupportCount();
      freqItemset.set.removeAll(cons.set);
      
      float antecedentSupport = getSupport(freqItemset, allSupports);  
      
      float confidence = numeratorSupport / antecedentSupport;
      if (confidence < minConfidence) {
        it.remove();
      }
      
      freqItemset.set.addAll(cons.set);
    }
  }

  private float getSupport(HashTreeItemset itemset,
      List<HashTree<String>> allSupports) {
    int k = itemset.size();
    
    if (allSupports.size() < k) {
      return 0;
    }
    
    HashTree<String> supportTree = allSupports.get(k - 1); // k-1 because indexing starts from 0, where as itemset size starts from 1.
    
    HashTreeItemset its = (HashTreeItemset) supportTree.get(itemset);
    
    if (its == null) {
      return 0;
    }
    
    return its.getSupportCount();
  }

  /**
   * Creates a new rule using the given consequent and by removing elements in the 
   * consequent to the antecedent. And adds the new rule to the given rules list.
   * @param freqItemset - the frequent itemset the rule is created from.
   * @param consequent - consequent of the rule
   * @param rules - the rules list to add the new rule to.
   */
  void addRuleFromConsequent(HashTreeItemset freqItemset, HashTreeItemset consequent,
      List<Rule> rules) {
    Rule r = new Rule(freqItemset, consequent);
    for (String item : consequent.items()) {
      r.getAntecedent().removeItem(item);
    }
    
    if (r.antecedent.size() == 0) {
      return;
    }
    rules.add(r);
  }

  public List<HashTreeItemset> generateFrequentItemset(List<HashTreeItemset> transactions, List<HashTree<String>> allSupports) {
    return generateFrequentItemset(transactions, allSupports, 0);
  }
  
  
  /**
   * Generate frequent itemset for the given transactions. Based on Algorithm 6.1 on page 337 in
   * "Introduction to Data Mining" by Pang-Ning Tan.
   * 
   * @param transactions
   */
  public List<HashTreeItemset> generateFrequentItemset(List<HashTreeItemset> transactions, List<HashTree<String>> allSupports, int kItemset) {
    int k = 0;
    // generate F1 (frequent item set with one element)
    List<HashTreeItemset> F1 = generateF1Candidates(transactions);
    countSupport(F1, transactions, k + 1); // k+1 because k0 is same as one item itemset.
    pruneCandidates(F1, transactions.size());
    List<HashTreeItemset> Fk_1 = null;

    List<HashTreeItemset> candidates = F1;
    
    // do
    do {
      allSupports.add(HashTree.makeTree((List<ItemSet<String>>)(List<?>)candidates, k+1));
      Fk_1 = candidates;
      k++;
      
      if (kItemset > 0 && k == kItemset) {
        return Fk_1;
      }
      
      if (verbose) {
        System.out.println("\nk = " + (k+1));
      }
      // generate candidate set
      candidates = generateCandidates(Fk_1, k, transactions.size());
      if (verbose) {
        System.out.println("Cadidate ItemsetSize (before pruning) = " + candidates.size());
        System.out.println("Cadidate Itemset (before pruning):\n\t" + candidates);
      }
      // count support
      countSupport(candidates, transactions, k + 1); // k+1 because k0 is same as one item itemset.

      // prune
      pruneCandidates(candidates, transactions.size());
      if (verbose) {
        System.out.println("Cadidate ItemsetSize (after pruning) = " + candidates.size());
        System.out.println("Cadidate Itemset (after pruning):\n\t" + candidates);
      }
    } while (candidates != null && !candidates.isEmpty()); // while candidate is not empty

    if (verbose) {
      System.out.println("Frequent Itemset Fk:\n\t" + Fk_1);
    }
    return Fk_1;
  }

  /**
   * Generate F1, one item, candidate itemset.
   * 
   * @param transactions
   * @return F1,one item, candidate itemset.
   */
  List<HashTreeItemset> generateF1Candidates(List<HashTreeItemset> transactions) {
    HashSet<String> items = new HashSet<String>();

    for (HashTreeItemset itemset : transactions) {
      for (String item : itemset.items()) {
        items.add(item);
      }
    }

    List<HashTreeItemset> candidates = new ArrayList<HashTreeItemset>(items.size());
    for (String item : items) {
      HashTreeItemset itemset = new HashTreeItemset();
      itemset.addItem(item);
      candidates.add(itemset);
    }

    return candidates;
  }

  /**
   * Generate frequent itemset candidates using Fk-1 X Fk-1 algorithm
   * 
   * @param arrayList
   * @param k
   * @return
   */
  List<HashTreeItemset> generateCandidates(List<HashTreeItemset> Fk_1, int k, float numTransactions) {
    List<HashTreeItemset> candidates = new ArrayList<HashTreeItemset>();
    Collections.sort(Fk_1);
    // create hash tree for quick lookup
    HashTree<String> supportTree = new HashTree<String>(k);// HashTree.<String>makeTree((List<Itemset>)candidates,
                                                           // itemsetSize);
    for (HashTreeItemset itemset : Fk_1) {
      supportTree.insert(itemset);
    }

    for (int i = 0; i < Fk_1.size(); i++) {
      for (int j = i + 1; j < Fk_1.size(); j++) {
        // if itemset has same elements apart from last then
        if (!Fk_1.get(i).equalsExcludeLastElement(Fk_1.get(j))) {
          break;
        }

        HashTreeItemset itemset = new HashTreeItemset();
        for (String s : Fk_1.get(i).items()) {
          itemset.addItem(s);
        }
        itemset.addItem(Fk_1.get(j).getItemAt(k - 1));

        // add only if all subsets, parents, have support > minSupport
        if (!hasParentMinSupoort(itemset, supportTree, numTransactions)) {
          continue;
        }

        candidates.add(itemset);
      }
    }

    return candidates;
  }

  /**
   * Check if all parents of a given itemset have the required minimum support.
   * 
   * @param itemset
   * @param supportTree
   * @param transactionSize
   * @return
   */
  boolean hasParentMinSupoort(HashTreeItemset itemset, HashTree<String> supportTree, float numTransactions) {
    String[] items = itemset.toArray();
    for (String s : items) {
      // for each parent get support
      itemset.removeItem(s);
      HashTreeItemset parent = (HashTreeItemset) supportTree.get(itemset);
      if (parent == null) {
        return false;
      }
      
      float sup = parent.getSupportCount() / numTransactions;
      // if parent not found in candidate list or parent.support < minSup return false
      if (parent == null || sup < minSupport) {
        itemset.addItem(s);
        return false;
      }

      itemset.addItem(s);
    }
    return true;
  }

  void countSupport(List<HashTreeItemset> candidates, List<HashTreeItemset> transactions,
      int itemsetSize) {
    if (candidates.isEmpty()) {
      return;
    }
    // create a hashtree
    HashTree<String> supportTree = new HashTree<String>(itemsetSize);// HashTree.<String>makeTree((List<Itemset>)candidates,
                                                                     // itemsetSize);
    for (HashTreeItemset itemset : candidates) {
      supportTree.insert(itemset);
    }


    // iterate over transactions and count support
    HashTreeItemset currentItemset = new HashTreeItemset();
    for (HashTreeItemset t : transactions) {
      countSupport(supportTree.root, t, itemsetSize, 0, 0, supportTree,
          currentItemset);
    }
  }

  private void countSupport(Node<String> curr, HashTreeItemset transaction,
      int itemsetSize, int depth, int startIndex, HashTree<String> supportTree,
      HashTreeItemset currentItemset) {
    if (depth == itemsetSize) {
      for (ItemSet<String> hti : curr.bucket) {
        if (((HashTreeItemset) hti).equals(currentItemset)) {
          ((HashTreeItemset) hti).incrementSupportCount();
        }
      }
      return;
    }

    for (int i = startIndex; i <= transaction.size() - (itemsetSize - depth); i++) {
      int key = supportTree.computeKey(transaction, i);
      Node<String> node = curr.children.get(key);
      if (node != null) {
        String currentItem = transaction.getItemAt(i);
        currentItemset.addItem(currentItem);

        countSupport(node, transaction, itemsetSize, depth + 1, i + 1,
            supportTree, currentItemset);

        currentItemset.removeItem(currentItem);
      }
    }

  }

  /**
   * Prunes frequent itemset candidate list based on the minSupport requirment
   * @param candidates - the candidate itemset list
   * @param numTransactions - total number of transactions.
   */
  void pruneCandidates(List<HashTreeItemset> candidates, float numTransactions) {
    Iterator<HashTreeItemset> it = candidates.iterator();
    while (it.hasNext()) {
      HashTreeItemset itemset = it.next();
      float sup = itemset.getSupportCount() / numTransactions;
      if (sup < minSupport) {
        it.remove();
      }
    }
  }

  public void printRules() {
    System.out.println("\nLearned Rules:");
    for (Rule r : rules) {
      System.out.println(r.toString());
    }
  }

}
