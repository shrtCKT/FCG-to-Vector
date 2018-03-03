package ml.cluster;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import ml.data.Attribute;
import ml.data.DataInstance;

public class BisectingKMeans {
  public static class ClusterComparator implements Comparator<Cluster> {
    @Override
    public int compare(Cluster o1, Cluster o2) {
      return o1.getSse() > o2.getSse() ? -1 : (o1.getSse() < o2.getSse() ? 1
          : 0);
    }
  }
  
  public static class ClusterSizeComparator implements Comparator<Cluster> {
    @Override
    public int compare(Cluster o1, Cluster o2) {
      return -1 * Integer.compare(o1.getMembers().cardinality(), o2.getMembers().cardinality());
    }
  }
  
  public static class ClusterSimilarityComparator implements Comparator<Cluster> {
    @Override
    public int compare(Cluster o1, Cluster o2) {
      return Double.compare(o1.getCosineAverageSimilarity(), o2.getCosineAverageSimilarity());
    }
  }
  
  public enum SplitType {
    SSE,                // Split based on cluster SSE.
    LARGEST_SIZE,       // Split the largest sized cluster.
    NORM_COSINE_SIM     // Splite based on the normalized vectors cosine similarity. 
                        // Assumes that the feature vectors are normalized
  }
  
  DistanceFunction distFn;
  List<Cluster> clusters;
  /***
   * When run in cutHeight mode, the culster algorithm, this holds the merge hieght on a dendrogram.
   */
  double[] mergeHieghts;
  
  public BisectingKMeans(DistanceFunction distFunction) {
    this.distFn = distFunction;
  }

  public List<Cluster> getClusters() {
    return clusters;
  }

  public void cluster(List<DataInstance> data, int k, int numTrials,
      List<Attribute> attributeList) {
    cluster(data, k, numTrials, attributeList, SplitType.SSE); 
  }
  
  public void cluster(List<DataInstance> data, int k, int numTrials,
      List<Attribute> attributeList, SplitType splitType) {
    cluster(data, k, numTrials, attributeList, splitType, false);
  }
  
  public void cluster(List<DataInstance> data, int k, int numTrials,
      List<Attribute> attributeList, SplitType splitType, boolean cutHeightMode) {
    BitSet initialCluster = new BitSet();
    // initially all data belong to one cluster.
    initialCluster.set(0, data.size(), true);
    
    mergeHieghts = null;
    if (cutHeightMode) {
      mergeHieghts = new double[data.size()];
    }

    PriorityQueue<Cluster> candidateClusters = null;
    if (splitType == SplitType.LARGEST_SIZE) {
      candidateClusters = new PriorityQueue<Cluster>(k, new ClusterSizeComparator());
    } else if (splitType == SplitType.NORM_COSINE_SIM) {
      candidateClusters = new PriorityQueue<Cluster>(k, new ClusterSimilarityComparator());
    } else {
      candidateClusters = new PriorityQueue<Cluster>(k, new ClusterComparator());
    }
    
    candidateClusters.add(new Cluster(0, data.get(0), initialCluster));

    while(candidateClusters.size() < k) {
      Cluster nextBisected = candidateClusters.poll();
      if (nextBisected.getMembers().isEmpty()) {
        continue;
      }
      List<Cluster> bisectionResult = null;
      
      double bestSSE = Double.POSITIVE_INFINITY;
      double bestCosSim = Double.NEGATIVE_INFINITY;
      for (int t = 0; t < numTrials; t++) {
        KMeans kmeans = new KMeans(distFn);
        kmeans.cluster(data, nextBisected.getMembers(), 2, attributeList,
            splitType != SplitType.NORM_COSINE_SIM);
        double currSSE = 0;
        double currCosSim = 0;
        if (splitType == SplitType.NORM_COSINE_SIM) {
          for (Cluster c : kmeans.getClusters()) {
            currCosSim = calcCosineAverageSimilarity(c, attributeList);
            c.setCosineAverageSimilarity(currCosSim);
          }
          if (bestCosSim < currCosSim) {
            bestCosSim = currCosSim;
            bisectionResult = kmeans.getClusters();
          }
        } else {
          for (Cluster c : kmeans.getClusters()) {
            currSSE += c.getSse();
          }
          if (currSSE < bestSSE) {
            bestSSE = currSSE;
            bisectionResult = kmeans.getClusters();
          }
        }
      }
      
      for (Cluster c : bisectionResult) {
        // System.out.println(c.getSse());
        if (c.getMembers().isEmpty()) {
          continue;
        }
        candidateClusters.add(c);
      }
      
      if (cutHeightMode) {
        int index = k - candidateClusters.size();
        mergeHieghts[index] = ((CosineDistance)distFn).similarity(bisectionResult.get(0).getCentroid(),
            bisectionResult.get(1).getCentroid(), attributeList);
      }
    }

    clusters = new ArrayList<Cluster>();
    int id = 0;
    for (Cluster c : candidateClusters) {
      c.setClusterID(id++);
      clusters.add(c);
    }
  }
  
  /***
   * Calculate the average cosine similarity within a cluster.
   * 
   * @param c the cluster.
   * @param attributeList list of attributes in datainstance.
   * @return The average cosine similarity.
   */
  private double calcCosineAverageSimilarity(Cluster c, List<Attribute> attributeList) {
    if (!this.distFn.getClass().equals(CosineDistance.class)) {
      throw new java.lang.IllegalArgumentException("Distance Function is not CosineDistance.");
    }
    CosineDistance cosDist = (CosineDistance) distFn;
    return Math.pow(cosDist.magnitude(c.centroid, attributeList), 2);
  }
  
  /***
   * Returns the various similarities for each cluster merges. 
   * This helps decide on the number of clusters, K.
   * 
   * @param data
   * @param attributeList
   * @return
   */
  public double[] cutHieght(List<DataInstance> data, int numIter, List<Attribute> attributeList,
      SplitType splitType) {
    cluster(data, data.size(), numIter, attributeList, splitType, true);
    return mergeHieghts;
  }
}
