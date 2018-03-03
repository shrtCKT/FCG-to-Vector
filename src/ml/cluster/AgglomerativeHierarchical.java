package ml.cluster;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ml.data.Attribute;
import ml.data.DataInstance;

public class AgglomerativeHierarchical {
  public enum ProximityMetric {
    SingleLink, CompleteLink, GroupAverage, IntraClusterSimilarity, CentroidSimilarity,
    UPGMASimilarity
  }
  
  public static class ClusterPair{
    final int firstCluster;
    final int secondCluster;
    double proximity;
    
    public ClusterPair(int firstCluster, int secondCluster) {
      super();
      this.firstCluster = firstCluster;
      this.secondCluster = secondCluster;
    }
    
    public ClusterPair(int firstCluster, int secondCluster, double proximity) {
      this(firstCluster, secondCluster);
      this.proximity = proximity;
    }

    public double getProximity() {
      return proximity;
    }
    public void setProximity(double proximity) {
      this.proximity = proximity;
    }
    public int getFirstCluster() {
      return firstCluster;
    }
    public int getSecondCluster() {
      return secondCluster;
    }

    @Override
    public int hashCode() {
      int result = 17;
      result = 31 * result + firstCluster;
      result = 31 * result + secondCluster;
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null) {
        return false;
      }
      if (!(obj instanceof ClusterPair)) {
        return false;
      }
      if (obj == this) {
        return true;
      }
      ClusterPair other = (ClusterPair)obj;
        
      return (this.firstCluster == other.firstCluster && this.secondCluster == other.secondCluster);
    }
    
  }
  
  // Map cluster id to cluster member mask.
  HashMap<Integer, Cluster> clusterMembership;
  List<Cluster> finalClusters;
  double[][] proxMatrix;
  /***
   * The distance function to be used. 
   */
  DistanceFunction distFn;
  /***
   * Similarity function to be used.
   */
  SimilarityFunction simFn;
  ProximityMetric proxMetric;
  /***
   * When run in cutHeight mode, the culster algorithm, this holds the merge hieght on a dendrogram.
   */
  double[] mergeHieghts;
  
  /***
   * Uses a distance function as opposed to a similarity function.
   * 
   * @param distFunction
   * @param proxMetric
   */
  public AgglomerativeHierarchical(DistanceFunction distFunction, ProximityMetric proxMetric) {
    this.distFn = distFunction;
    this.simFn = null;
    this.proxMetric = proxMetric;
  }
  
  /***
   * Uses a similarity function as opposed to a distance function.
   * @param simFunction
   * @param proxMetric
   */
  public AgglomerativeHierarchical(SimilarityFunction simFunction, ProximityMetric proxMetric) {
    this.distFn = null;
    this.simFn = simFunction;
    this.proxMetric = proxMetric;
  }

  public List<Cluster> getClusters() {
    return finalClusters;
  }

  public void cluster(List<DataInstance> data, int k, List<Attribute> attributeList) {
    cluster(data, k, attributeList, false);
  }
  
  public void cluster(List<DataInstance> data, int k, List<Attribute> attributeList, 
      boolean cutHieghtMode) {
    
    mergeHieghts = null;
    if (cutHieghtMode) {
      mergeHieghts = new double[data.size()];
    }
    
    // Create N clusters each with one member point 
    clusterMembership = new HashMap<Integer, Cluster>();
    for (int i = 0; i < data.size(); i++) {
      Cluster cMemebers = new Cluster(i, data.get(i));
      cMemebers.getMembers().set(i);
      clusterMembership.put(i, cMemebers);
    }
    
    initializeProximityMatrix(data, attributeList);
    int numClusters = data.size();
    int count = 0;
    do {
      ClusterPair closestC = null;
      if (simFn != null) {
        closestC = pickMostSimilarClusters();
      } else {
        closestC = pickClosestClusters();
      }
      // merge closest two clusters
      if (cutHieghtMode) {
        mergeHieghts[count++] = closestC.getProximity();
      }
      mergeClusters(closestC.getFirstCluster(), closestC.getSecondCluster(), data, attributeList);
      numClusters--;
    } while(numClusters > k);
    
    finalClusters = new ArrayList<Cluster>();
    for (Map.Entry<Integer, Cluster> me : clusterMembership.entrySet()) {
      finalClusters.add(me.getValue());
    }
  }

  private void initializeProximityMatrix(List<DataInstance> data, List<Attribute> attributeList) {
    proxMatrix = new double[data.size()][data.size()];
    for (int i = 0; i < data.size(); i++) {
      for (int j = i; j < data.size(); j++) {
        if (simFn == null) {
          if (i == j) {
            proxMatrix[i][j] = 0.0;
            continue;
          }
          proxMatrix[i][j] = distFn.distance(data.get(i), data.get(j), attributeList);
        } else {
          proxMatrix[i][j] = simFn.similarity(data.get(i), data.get(j), attributeList);
        }
        proxMatrix[j][i] = proxMatrix[i][j];
      }
    }
  }

  private ClusterPair pickClosestClusters() {
    double minProx = Double.POSITIVE_INFINITY;
    ClusterPair pair = null;
    for (Integer i : clusterMembership.keySet()) {
      for (Integer j : clusterMembership.keySet()) {
        if (j <= i) {
          continue;
        }
        if (proxMatrix[i][j] < minProx) {
          minProx = proxMatrix[i][j];
          pair = new ClusterPair(i,j, minProx);
        }
      }
    }
    
    return pair;
  }
  
  private ClusterPair pickMostSimilarClusters() {
    double maxSim = Double.NEGATIVE_INFINITY;
    ClusterPair pair = null;
    for (Integer i : clusterMembership.keySet()) {
      for (Integer j : clusterMembership.keySet()) {
        if (j <= i) {
          continue;
        }
        if (proxMatrix[i][j] > maxSim) {
          maxSim = proxMatrix[i][j];
          pair = new ClusterPair(i,j, maxSim);
        }
      }
    }
    
    return pair;
  }

  private double calcProximity(int cluster1, int cluster2, List<DataInstance> data, List<Attribute> attributeList) {
    if (this.proxMetric == ProximityMetric.SingleLink) {
      return singleLinkProximity(cluster1, cluster2, data, attributeList);
    } else if (this.proxMetric == ProximityMetric.CompleteLink) {
      return completeLinkProximity(cluster1, cluster2, data, attributeList);
    } else if (this.proxMetric == ProximityMetric.GroupAverage) {
      return groupAverageProximity(cluster1, cluster2, data, attributeList);
    } else if (this.proxMetric == ProximityMetric.IntraClusterSimilarity) {
      return intraClusterSimilarity(cluster1, cluster2, data, attributeList);
    } else if (this.proxMetric == ProximityMetric.CentroidSimilarity) {
      return centroidSimilarity(cluster1, cluster2, data, attributeList);
    } else {
      return upgmaSimilarity(cluster1, cluster2, data, attributeList);
    }
  }


  /***
   * Defines the proximity between two clusters as the distance beweet the closest two points that
   * are in different clusters.
   * 
   * @param cluster1 cluster ID of cluster 1.
   * @param cluster2 cluster ID of cluster 2.
   * @param data the dataset.
   * @param attributeList the attribute list. 
   * @return the proximity between the two clusters. 
   */
  private double singleLinkProximity(int cluster1, int cluster2, List<DataInstance> data, List<Attribute> attributeList) {
    double minProx = Double.POSITIVE_INFINITY;
    BitSet c1 = clusterMembership.get(cluster1).getMembers();
    BitSet c2 = clusterMembership.get(cluster2).getMembers();
    
    for (int i = c1.nextSetBit(0); i > -1; i = c1.nextSetBit(i + 1)) {
      for (int j = c2.nextSetBit(0); j > -1; j = c2.nextSetBit(j + 1)) {
        double dist = distFn.distance(data.get(i), data.get(j), attributeList);
        if (dist < minProx) {
          minProx = dist;
        }
      }
    }
    
    return minProx;
  }

  /***
   * Defines the proximity between two clusters as the distance beweet the farthes two points that
   * are in different clusters.
   * 
   * @param cluster1 cluster ID of cluster 1.
   * @param cluster2 cluster ID of cluster 2.
   * @param data the dataset.
   * @param attributeList the attribute list. 
   * @return the proximity between the two clusters. 
   */
  private double completeLinkProximity(int cluster1, int cluster2, List<DataInstance> data, List<Attribute> attributeList) {
    double maxProx = Double.NEGATIVE_INFINITY;
    BitSet c1 = clusterMembership.get(cluster1).getMembers();
    BitSet c2 = clusterMembership.get(cluster2).getMembers();
    
    for (int i = c1.nextSetBit(0); i > -1; i = c1.nextSetBit(i + 1)) {
      for (int j = c2.nextSetBit(0); j > -1; j = c2.nextSetBit(j + 1)) {
        double dist = distFn.distance(data.get(i), data.get(j), attributeList);
        if (maxProx < dist) {
          maxProx = dist;
        }
      }
    }
    
    return maxProx;
  }

  /***
   * Defines the proximity between two clusters as the average pairwise distance between the 
   * memebers of the two clusters.
   * 
   * @param cluster1 cluster ID of cluster 1.
   * @param cluster2 cluster ID of cluster 2.
   * @param data the dataset.
   * @param attributeList the attribute list. 
   * @return the proximity between the two clusters. 
   */
  private double groupAverageProximity(int cluster1, int cluster2, List<DataInstance> data, 
      List<Attribute> attributeList) {
    double totalProx = 0;
    int count = 0;
    BitSet c1 = clusterMembership.get(cluster1).getMembers();
    BitSet c2 = clusterMembership.get(cluster2).getMembers();
    
    for (int i = c1.nextSetBit(0); i > -1; i = c1.nextSetBit(i + 1)) {
      for (int j = c2.nextSetBit(0); j > -1; j = c2.nextSetBit(j + 1)) {
        totalProx += distFn.distance(data.get(i), data.get(j), attributeList);
        count++;
      }
    }
    
    return totalProx/count;
  }

  private double intraClusterSimilarity(int cluster1, int cluster2, List<DataInstance> data, 
      List<Attribute> attributeList) {
    // If cluster Z = merge of Cluster X and Y 
    // and Sim(A) = SUM_over_memebers_d_of_A(d, CentroidOf_A), 
    // Then intraClusterSimilarity = Sim(Z) - (Sim(X) + Sim(Y))
    double simC1 = intraClusterSimilarity(clusterMembership.get(cluster1).getCentroid(),
        clusterMembership.get(cluster1).getMembers(), data, attributeList);
    double simC2 = intraClusterSimilarity(clusterMembership.get(cluster2).getCentroid(),
        clusterMembership.get(cluster2).getMembers(), data, attributeList);
    
    BitSet newClusterMembers = new BitSet();
    newClusterMembers.or(clusterMembership.get(cluster1).getMembers());
    newClusterMembers.or(clusterMembership.get(cluster2).getMembers());
    DataInstance newClusterCentroid = calculateCentroid(data, newClusterMembers, attributeList);
    
    double simNewC = intraClusterSimilarity(newClusterCentroid, newClusterMembers, data,
        attributeList);
    
    return simNewC - (simC1 + simC2);
  }
  
  private double intraClusterSimilarity(DataInstance centroid, BitSet clusterMembers, 
      List<DataInstance> data, List<Attribute> attributeList) {
    
    CosineDistance cosFn = (CosineDistance) simFn;
    double magnitude = cosFn.magnitude(centroid, attributeList);
    double dotSum = 0.0;
    for (int i = clusterMembers.nextSetBit(0); i > -1; i = clusterMembers.nextSetBit(i + 1)) {
      dotSum += cosFn.dotProduct(data.get(i), centroid, attributeList);
    }
    dotSum = magnitude == 0 ? 0 : dotSum / magnitude;
    return dotSum;
  }
  
  private double centroidSimilarity(int cluster1, int cluster2, List<DataInstance> data,
      List<Attribute> attributeList) {
    return simFn.similarity(clusterMembership.get(cluster1).getCentroid(),
        clusterMembership.get(cluster2).getCentroid(), attributeList);
  }
  
  private double upgmaSimilarity(int cluster1, int cluster2, List<DataInstance> data,
      List<Attribute> attributeList) {
    double upgma = 0;
    for (int i = clusterMembership.get(cluster1).getMembers().nextSetBit(0); i > -1; 
        i = clusterMembership.get(cluster1).getMembers().nextSetBit(i + 1)) {
      for (int j = clusterMembership.get(cluster2).getMembers().nextSetBit(0); j > -1; 
          j = clusterMembership.get(cluster2).getMembers().nextSetBit(j + 1)) {
        upgma += simFn.similarity(data.get(i), data.get(j), attributeList);
      }
    }
    
    double size = clusterMembership.get(cluster1).getMembers().cardinality() *
        clusterMembership.get(cluster2).getMembers().cardinality();
    
    upgma = (size == 0 ? 0 : upgma / size);
    
    return upgma;
  }
  
  /**
   * Merges the second cluster to the first and names the new cluster with 
   * the same id as firstCluster.
   * @param firstCluster cluster id of first cluster.
   * @param secondCluster cluster id of second cluster.
   * @param data 
   */
  private void mergeClusters(int firstCluster, int secondCluster, List<DataInstance> data, List<Attribute> attributeList) {
    // merge
    clusterMembership.get(firstCluster).getMembers().or(clusterMembership.get(secondCluster).getMembers());
    
    // Recalculate centroid.
    if (proxMetric == ProximityMetric.IntraClusterSimilarity ||
        proxMetric == ProximityMetric.CentroidSimilarity ||
        proxMetric == ProximityMetric.UPGMASimilarity) {
      DataInstance newCentroid = calculateCentroid(
          data, clusterMembership.get(firstCluster).getMembers(), attributeList);
      
      clusterMembership.get(firstCluster).setCentroid(newCentroid);
//      System.err.printf("Merge %d %s\n and %d %s \nCetroid = %s\n", firstCluster, 
//          clusterMembership.get(firstCluster).getCentroid(), secondCluster, 
//          clusterMembership.get(secondCluster).getCentroid(),
//          newCentroid);
    }
    
    // Remove second
    clusterMembership.remove(secondCluster);
    // Recalculate proximity form all clusters to firstCluster(i.e. new cluster).
    for (Integer i : clusterMembership.keySet()) {
      if (i == firstCluster) {
        if (proxMetric == ProximityMetric.IntraClusterSimilarity ||
            proxMetric == ProximityMetric.CentroidSimilarity ||
            proxMetric == ProximityMetric.UPGMASimilarity) {
          proxMatrix[i][firstCluster] = calcProximity(i, firstCluster, data, attributeList);
          proxMatrix[firstCluster][i] = proxMatrix[i][firstCluster];
        } else {
          proxMatrix[i][firstCluster] = 0; // distance of a cluster to it's self is zero.
          proxMatrix[firstCluster][i] = proxMatrix[i][firstCluster];
        }
      } else {
        proxMatrix[i][firstCluster] = calcProximity(i, firstCluster, data, attributeList);
        proxMatrix[firstCluster][i] = proxMatrix[i][firstCluster];
      } // TODO What about i == secondCluster
    }
  }


  /**
   * Computes the cluster centroids.
   * 
   * @param centroids
   * @param data
   * @param mask
   * @param clusterMembership
   * @param attributeList
   */
  public static DataInstance calculateCentroid(final List<DataInstance> data,
      final BitSet clusterMemebers, final List<Attribute> attributeList) {
    // Initialize the new centroids to 0
    DataInstance newCentroid = new DataInstance();
    for (Map.Entry<Integer, Object> me : data.get(0).getAttributes()
        .entrySet()) {
      newCentroid.setAttributeValueAt(me.getKey(), 0.0);
    }
  
    // Calculate each attribute's sum.
    int count = 0;
    for (int i = clusterMemebers.nextSetBit(0); i > -1; i = clusterMemebers.nextSetBit(i + 1)) {
      count++;
      for (Map.Entry<Integer, Object> me : data.get(0).getAttributes()
          .entrySet()) {
        int attribIndex = me.getKey();
        // Skip Non-Continuous attributes
        if (attributeList.get(attribIndex).getType() != Attribute.Type.CONTINUOUS) {
          continue;
        }
  
        double sum = (Double) newCentroid.getAttributeValueAt(attribIndex)
                    + (Double) data.get(i).getAttributeValueAt(attribIndex);
        newCentroid.setAttributeValueAt(attribIndex, sum);
      }
    }
  
    // Calculate each attribute's average.
    for (Map.Entry<Integer, Object> me : data.get(0).getAttributes().entrySet()) {
      int attributeIndex = me.getKey();
      newCentroid.setAttributeValueAt(
              attributeIndex,
              (count == 0 ? 0 : (Double) newCentroid.getAttributeValueAt(attributeIndex)/ count));
    
    }
    return newCentroid;
  }

  /***
   * Returns the various similarities for each cluster merges. 
   * This helps decide on the number of clusters, K.
   * 
   * @param data
   * @param attributeList
   * @return
   */
  public double[] cutHieght(List<DataInstance> data, List<Attribute> attributeList) {
    cluster(data, 1, attributeList, true);
    return mergeHieghts;
  }

}