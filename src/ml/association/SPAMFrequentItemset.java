package ml.association;

import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/***
 * Implementation of Ayres, Jay, et al. "Sequential pattern mining using a bitmap representation."
 * The SPAM algorithm, proposed in the paper, uses bitmap representation and sequence tree to 
 * efficiently mines for frequent itemsets.
 *  
 *  Current implementation only supports one customer datasets.
 *  
 * @author mehadi
 *
 */
public class SPAMFrequentItemset {
  public static final int ALL_GAPS = -1;

  public static final int LEVEL_0 = 0;  // No verbose comments.
  public static final int LEVEL_1 = 1;  // Level 1 verbose comments only.
  public static final int LEVEL_2 = 2;  // Level 1 and 2 verbose comments only.
  
  private double minSupport;
  
  /***
   * Maps ID or index to the item.
   */
  private Map<Integer, String> itemIndexMap;
  
  /***
   * A vertical bitmap representation of each item in the itemset.
   */
  private BitSet[] itemBitmaps;
  
  /***
   * The number of transactions
   */
  private int transactionCount;
  
  /***
   * Used to save the frequent itemset sequences of required size.
   */
  private List<Sequence> frequentSequence;
  /***
   * The size of the smallest frequent sequences to be saved.
   */
  private int minSequenceSize;
  /***
   * The size of the largest frequent sequences to be saved.
   */
  private int maxSequenceSize;
  /***
   * The number of gaps, trasactions, allowed between two sequence items.
   * For example: Let
   *    seq1 = ({a},{b})
   *    seq2 = ({a,c}, {b,c}, {e})
   *    seq3 = ({a,c}, {f}, {b,c})
   *    
   *    If numAllowedGaps=0 then only seq2 contains seq1.
   *    If numAllowedGaps>=1 then both seq2, seq3 contain seq1.
   *    
   *    Note: this only applies to S-Step.
   */
  private int numAllowedGaps = ALL_GAPS;

  private int verbose = LEVEL_0;
  
  public SPAMFrequentItemset(double minSupport) {
    this(minSupport, 0, Integer.MAX_VALUE, SPAMFrequentItemset.ALL_GAPS);
  }
  
  public SPAMFrequentItemset(double minSupport, int minSequenceSize, int maxSequenceSize, 
      int numAllowedGaps) {
    this.minSupport = minSupport;
    this.minSequenceSize = minSequenceSize;
    this.maxSequenceSize = maxSequenceSize;
    this.numAllowedGaps = numAllowedGaps;
  }

  public void setVerboseLevel(int level) {
    this.verbose = level;
  }
  
  /***
   * This method is used for testing. In normal operation transactionCount is set by
   * Initialize() method.
   * 
   * @param count the number of transactions
   */
  protected void setTransactionCount(int count) {
    transactionCount = count;
  }
  
  private void Initialize(List<HashTreeItemset> transactions) {
    transactionCount = transactions.size();
    
    // Maps index to item. Used later to retrieve item using index.
    itemIndexMap = new HashMap<Integer, String>();
    
    // Maps item to index. Used to avoid duplicates and index lookup.
    Map<String, Integer> tmpMapNametoIndex = new HashMap<String, Integer>();
    
    mapItemIndex(transactions, itemIndexMap, tmpMapNametoIndex);
    
    itemBitmaps = CreateItemBitmap(transactions, itemIndexMap, tmpMapNametoIndex);
  }

  /***
   * Creates the Original Bitmap for each item based on the given transactions.
   * Current Implementation only supports single custom transactions.
   * @param transactions
   */
  protected BitSet[] CreateItemBitmap(final List<HashTreeItemset> transactions, 
      final Map<Integer, String> indexToItemMap, final Map<String, Integer> itemToIndexMap) {
    BitSet[] itemBitmaps = new BitSet[itemToIndexMap.size()];
    for (int j = 0; j < transactions.size(); j++) {
      for (String item : transactions.get(j).items()) {
        int i = itemToIndexMap.get(item);
        if (itemBitmaps[i] == null) {
          itemBitmaps[i] = new BitSet(transactions.size());
        }
        itemBitmaps[i].set(j);
      }
    }
    return itemBitmaps;
  }

  protected void mapItemIndex(List<HashTreeItemset> transactions, Map<Integer, String> indexToItemMap, 
      Map<String, Integer> itemToIndexMap) {
    // Identify all the unique items.
    int index = 0;
    for (HashTreeItemset trans : transactions) {
      for (String item : trans.items()) {
        if (itemToIndexMap.containsKey(item)) {
          continue;
        }
        itemToIndexMap.put(item, index);
        indexToItemMap.put(index, item);
        index++;
      }
    }
  }

  /***
   * Finds frequent sequences in the list of given transactions using SPAM algorithm.
   * Currently only supports one customer transactions.
   * 
   * @param transactions - List of transactions
   * @return A list of most frequent sequences.
   */
  public List<Sequence> MineFrequentItemset(List<HashTreeItemset> transactions) {
    frequentSequence = new LinkedList<Sequence>();
    
    // Create the original Bitmap for each item in the transactions.
    Initialize(transactions);
    
    BitSet sequenceExtendedCandidate = new BitSet();
    
    for (Integer index : itemIndexMap.keySet()) {
      sequenceExtendedCandidate.set(index);
    }

    for (int i = sequenceExtendedCandidate.nextSetBit(0); i > -1;
        i = sequenceExtendedCandidate.nextSetBit(i + 1)) {
      if (support(itemBitmaps[i]) >= minSupport) {
        // If support for sequence of length 1 is more than minSupport
        
        // Candidate i extensions are elements > i
        BitSet mask = new BitSet();
        mask.set(i+1, itemIndexMap.size());
        mask.and(sequenceExtendedCandidate);
        
        Sequence seq = new Sequence();
        HashTreeItemset itemSet = new HashTreeItemset();
        itemSet.addItem(itemIndexMap.get(i));
        seq.addItemset(itemSet);
        
        SPAMRecursive(seq, itemBitmaps[i], sequenceExtendedCandidate, mask);
      }
    }
    //initialSet.set(0, transactions.size());
    
    if (verbose >= LEVEL_1) {
      System.out.println("FrequentSequenceSize= " + frequentSequence.size());
    }
    return frequentSequence;
  }
  
  /***
   * Implementation of DFS SPAM algorithm.
   * 
   * @param nodeSequence The sequence at current node.
   * @param nodeBitmap vertical bitmap representation of the nodeSequence.
   * @param sequenceExtendedCandidate Candidate items for s-step.
   * @param itemExtendedCandidate Candidate items for i-step.
   */
  protected void SPAMRecursive(Sequence nodeSequence, BitSet nodeBitmap, 
      BitSet sequenceExtendedCandidate, BitSet itemExtendedCandidate) {
    if (verbose >= LEVEL_2) {
      System.out.println(frequentSequence.size());
    }
    
    BitSet stmp = new BitSet();    // s extension candidates 
    BitSet itmp = new BitSet();    // i extension candidates
    // For each item in Sequence-Extended Candidate list
    for (int i = sequenceExtendedCandidate.nextSetBit(0); i > -1;
        i = sequenceExtendedCandidate.nextSetBit(i + 1)) {
      
      if (support(sStep(nodeBitmap, itemBitmaps[i])) >= minSupport) {
        // If support is more than minSupport add to candidate list to be passed to for child node.
        stmp.set(i);
      }
    }
    // For candidate s-extension perform the s-step and DFS with pruning 
    for (int i = stmp.nextSetBit(0); i > -1; i = stmp.nextSetBit(i + 1)) {
      BitSet s_extended = sStep(nodeBitmap, itemBitmaps[i]);
      Sequence sSeq = sequenceExtend(nodeSequence, itemIndexMap.get(i));
      sSeq.setSupport(support(s_extended));

      if (sSeq.size() > maxSequenceSize) {
        continue;
      }

      if (sSeq.size() >= minSequenceSize) {
        // Save the identified frequent sequence.
        frequentSequence.add(sSeq);
      }

      // All items in Stmp greater than i.
      BitSet sTmpAfterI = new BitSet();
      sTmpAfterI.set(i + 1, stmp.length()); // to use as mask
      sTmpAfterI.and(stmp);
      
      SPAMRecursive(sSeq, s_extended, stmp, sTmpAfterI); 
    }
    
    // For each item in Item-Extended Candidate list
    for (int i = itemExtendedCandidate.nextSetBit(0); i > -1;
        i = itemExtendedCandidate.nextSetBit(i +1)) {
      if (support(iStep(nodeBitmap, itemBitmaps[i])) >= minSupport) {
        // If support is more than minSupport add to candidate list to be passed to for child node.
        itmp.set(i);
      }
    }
    // For candidate i-extension perform the i-step and DFS with pruning 
    for (int i = itmp.nextSetBit(0); i > -1; i = itmp.nextSetBit(i + 1)) {
      BitSet i_extended = iStep(nodeBitmap, itemBitmaps[i]);
      Sequence iSeq = itemExtend(nodeSequence, itemIndexMap.get(i));
      iSeq.setSupport(support(i_extended));
      
      if (iSeq.size() > maxSequenceSize) {
        continue;
      }
      
      if (iSeq.size() >= minSequenceSize) {
        // Save the identified frequent sequence.
        frequentSequence.add(iSeq);
      }
      
      // All items in Stmp greater than i.
      BitSet iTmpAfterI = new BitSet();
      iTmpAfterI.set(i + 1, itmp.length()); // to use as mask
      iTmpAfterI.and(itmp);
      
      SPAMRecursive(iSeq, i_extended, stmp, iTmpAfterI);
    }
  }
  
  /***
   * Creates a new through the sequence extension step by adding an itemset of one element {item}  
   * to the originalSequence.
   * 
   * @param originalSequence The original sequence.
   * @param item The item to be added.
   * @return The new sequence which is the result of sequence extending originalSequence.
   */
  protected Sequence sequenceExtend(Sequence originalSequence, String item) {
    Sequence newSeq = new Sequence(originalSequence);
    HashTreeItemset newItemset = new HashTreeItemset();
    newItemset.addItem(item);
    
    newSeq.addItemset(newItemset);
    
    return newSeq;
  }

  /***
   * Performs the sequence extension step(s-step) on the bitmap representation.
   * 
   * @param nodeBitmap The bitmap representation of the original sequence.
   * @param itemBitMap The bitmap representation of the item to be added.
   * @return The resulting bitmap of performing s-step.
   */
  protected BitSet sStep(BitSet nodeBitmap, BitSet itemBitMap) {
    // change {a} -> ({a})s
    BitSet as = new BitSet(nodeBitmap.length());
    if (numAllowedGaps == ALL_GAPS) {
      int first_set_bit = nodeBitmap.nextSetBit(0);
      if (first_set_bit > -1) {
        as.set(first_set_bit + 1, Math.max(nodeBitmap.length(), itemBitMap.length()));
      }
    } else {
      for (int i = nodeBitmap.previousSetBit(nodeBitmap.length()); i > -1; 
          i = nodeBitmap.previousSetBit(i-1)) {
          int fromIndex = i + 1;
          int toIndex = Math.min(fromIndex + numAllowedGaps, transactionCount);
          as.set(fromIndex, toIndex + 1);
      }
    }
    
    // ({a})s and {b}
    as.and(itemBitMap);
    
    return as;
  }
  
  /***
   * Creates a new through the item extension step by adding an item of to the last itemset in the 
   * originalSequence.
   * 
   * @param originalSequence The original sequence.
   * @param item The item to be added.
   * @return The new sequence which is the result of item extending originalSequence.
   */
  protected Sequence itemExtend(Sequence originalSequence, String item) {
    HashTreeItemset lastItemset = null;
    Sequence newSeq = new Sequence(originalSequence);
    if (newSeq.size() == 0) {
      lastItemset = new HashTreeItemset();
      newSeq.addItemset(lastItemset);
    } else {
      lastItemset = (HashTreeItemset) originalSequence.getElementAt(originalSequence.size() - 1);
    }
    
    lastItemset.addItem(item);
    return newSeq;
  }
  
  /***
   * Performs the item extension step(i-step) on the bitmap representation.
   * 
   * @param nodeBitmap The bitmap representation of the original sequence.
   * @param itemBitMap The bitmap representation of the item to be added.
   * @return The resulting bitmap of performing i-step.
   */
  protected BitSet iStep(BitSet nodeBitmap, BitSet itemBitMap) {
    BitSet ret = new BitSet();
    ret.or(nodeBitmap);
    ret.and(itemBitMap);
    return ret;
  }

  /***
   * Counts the support in the given sequenceBitmap.
   * @param sequenceBitmap the bitmap representation of the sequence.
   * @return The support of sequenceBitmap.
   */
  protected double support(BitSet sequenceBitmap) {
    return transactionCount == 0 ? 0.0 : sequenceBitmap.cardinality() / (double)transactionCount;
  }
}
