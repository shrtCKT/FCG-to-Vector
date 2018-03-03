package ml.cluster;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Random;

import ml.data.Attribute;
import ml.data.DataInstance;

public class KMedoids {
  /***
   * Initial centroids chosing strategy.
   * @author meha
   *
   */
  public enum CentroidInitialization{
    RANDOM,  // chosen at random  
    PLUSPLUS // chosen at random with probability proportional to  distance to the closest center.
             // based on Arthur, David, and Sergei Vassilvitskii. 
             // "k-means++: The advantages of careful seeding." 
  }
  public Random rng;
  DistanceFunction distFn;
  List<Cluster> clusters;
  int maxIter; 
  CentroidInitialization centerInitializationType;

  public KMedoids(DistanceFunction distFunction) {
    this(distFunction, null, CentroidInitialization.RANDOM, 5000);
  }
  
  public KMedoids(DistanceFunction distFunction, CentroidInitialization centerInitializationType,
      int maxIter) {
    this(distFunction, null, centerInitializationType, maxIter);
  }
  
  public KMedoids(DistanceFunction distFunction, List<Cluster> clusters, int maxIter) {
    this(distFunction, clusters, CentroidInitialization.RANDOM, maxIter);
  }
  
  public KMedoids(DistanceFunction distFunction, List<Cluster> clusters,
      CentroidInitialization centerInitializationType, int maxIter) {
    long seed = Long.getLong("seed", System.currentTimeMillis());
    System.out.println("Seed=" + seed);
    this.rng = new Random(seed);
    this.distFn = distFunction;
    this.clusters = clusters;
    this.centerInitializationType = centerInitializationType;
    this.maxIter = maxIter;
  }

  public List<Cluster> getClusters() {
    return clusters;
  }

  /**
   * Clusters all of the given data points.
   * 
   * @param data data points.
   * @param k the number of final clusters.
   * @param attributeList data point attribute list.
   */
  public void cluster(List<DataInstance> data, int k,
      List<Attribute> attributeList) {
    BitSet allDataMask = new BitSet(data.size());
    allDataMask.set(0, data.size(), true);
    cluster(data, allDataMask, k, attributeList, true);
  }
  
  public void cluster(List<DataInstance> data, int k,
      List<Attribute> attributeList, boolean calcSSE) {
    BitSet allDataMask = new BitSet(data.size());
    allDataMask.set(0, data.size(), true);
    cluster(data, allDataMask, k, attributeList, calcSSE);
  }

  /**
   * Clusters data points identified by mask bit vector.
   * 
   * @param data data points.
   * @param mask bit vector used identify which data instance need to be clustered.
   * @param k the number of final clusters.
   * @param attributeList data point attribute list.
   */
  public void cluster(final List<DataInstance> data, final BitSet mask, final int k,
      final List<Attribute> attributeList, boolean calcSSE) {
    // Create a shadow array for data to store cluster membership
    int[] clusterMembership = new int[data.size()];
    for (int i = mask.nextSetBit(0); i > -1; i = mask.nextSetBit(i + 1)) {
      clusterMembership[i] = -1;
    }
    // A shadow array of bitset. If clusterMembershipBitSets[i].get(j) is True then data instance j is a 
    // member of cluster i.
    BitSet[] clusterMembershipBitSets = new BitSet[k];
    for (int i = 0; i < k; i++) {
      clusterMembershipBitSets[i] = new BitSet();
    }
    
    
    // pick k random data points and set them as centroid
    if (clusters == null) {
      if (centerInitializationType == CentroidInitialization.PLUSPLUS) {
        clusters = initialMedoidsPlusPlus(data, mask, k, attributeList);
      } else {
        clusters = initialMedoidsUniformRandom(data, mask, k);
      }
    }
    
    int maxI = maxIter;
    boolean clusterMemberShipChange = true;
    do {
      // assigned data points to clusters
      clusterMemberShipChange =
          assignToCluster(data, mask, clusterMembership, attributeList, clusterMembershipBitSets);

      // recompute centroid
      recomputeMedoids(data, mask, clusterMembershipBitSets, attributeList);
      maxI--;
    } while (clusterMemberShipChange && maxI > 0);// while stop condition not fulfilled

    if (calcSSE) {
      calcSSE(data, mask, clusterMembership, attributeList);
    }
    finalizeClusterMemebership(clusterMembership, mask);
  }

  /***
   * Recomputes each clusters medoid by picking the member with the least average distance
   * to all other members of a cluster to be the new medoid.
   * 
   * @param data data points.
   * @param mask bit vector used identify which data instance need to be clustered.
   * @param clusterMembershipBitSets an array of bitsets where clusterMembershipBitSets[i].get(j) 
   * is True then data instance j is a member of cluster i.
   * @param attributeList data point attribute list.
   */
  private void recomputeMedoids(List<DataInstance> data, BitSet mask,
      BitSet[] clusterMembershipBitSets, List<Attribute> attributeList) {
    // For each cluster and for each memeber of that cluster, compute the average proximity to
    // all other memebers of that cluster. And pick the member with the least average distance
    // to be the new medoid.
    
    // For each cluster
    for (int c = 0; c < clusterMembershipBitSets.length; c++) { 
      double minAveProximity = Double.POSITIVE_INFINITY;
      BitSet clusterElements = clusterMembershipBitSets[c];
      int medoidIndex = -1;
      // For each elements of this cluster.
      for (int i = clusterElements.nextSetBit(0); i > -1; i = clusterElements.nextSetBit(i+1)) {
        double aveProximity = 0.0; 
        for (int j = clusterElements.nextSetBit(0); j > -1; j = clusterElements.nextSetBit(j+1)) {
          if (i == j) {
            continue;
          }
          aveProximity += distFn.distance(data.get(i), data.get(j), attributeList);
        }
        aveProximity = clusterElements.isEmpty() ? 0.0 : aveProximity / clusterElements.size();
        
        if (aveProximity <= minAveProximity) {
          minAveProximity = aveProximity;
          medoidIndex = i;
        }
      }
      
      // Change cluster medoid
      clusters.get(c).setCentroid(data.get(medoidIndex));
    }
    
  }

  /**
   * Creates initial cluster medoids by selecteing data point uniformly at random.
   * 
   * @param data all data points.
   * @param mask bit vector used identify which data instance need to be clustered.
   * @param k the number of final clusters.
   * @return List of initial cluster centroids.
   */
  private List<Cluster> initialMedoidsUniformRandom(List<DataInstance> data, BitSet mask,
      int k) {
    List<Cluster> medoids = new ArrayList<Cluster>(k);

    int c = 0;
    BitSet dupDetect = new BitSet(mask.cardinality());
    while (c < k) {
      int offset = rng.nextInt(mask.cardinality());
      if (dupDetect.get(offset)) {
        continue;
      }
      dupDetect.set(offset);
      c++;

      int start = mask.nextSetBit(0);
      int i = start;
      for (; i > -1 && i < start + offset; i =
          mask.nextSetBit(i + 1));
      medoids.add(new Cluster(c, new DataInstance(data.get(i))));
    }

    System.out.println("STATUS: Initialized Medoids");
    return medoids;
  }
  
  /**
   * Creates initial cluster medoids. The first center point chosen uniformly at random from all
   * data points, the next points chosen with probability proportional to distance square of the
   * center point to the closest center(chosen before) divided by distance square of all data points 
   * to the closes centers chosen so far. Based on the paper 
   * Arthur, David, and Sergei Vassilvitskii. "k-means++: The advantages of careful seeding." 
   * Proceedings of the eighteenth annual ACM-SIAM symposium on Discrete algorithms. 
   * Society for Industrial and Applied Mathematics, 2007.
   * 
   * @param data all data points.
   * @param mask bit vector used identify which data instance need to be clustered.
   * @param k the number of final clusters.
   * @return List of initial cluster centroids.
   */
  private List<Cluster> initialMedoidsPlusPlus(List<DataInstance> data, BitSet mask,
      int k, List<Attribute> attributeList) {
    List<Cluster> medoids = new ArrayList<Cluster>(k);

    int c = 0;
    BitSet dupDetect = new BitSet(mask.cardinality());
    while (c < k) {
      int offset = rng.nextInt(mask.cardinality());
      if (dupDetect.get(offset)) {
        continue;
      }
      
      int start = mask.nextSetBit(0);
      int i = start;
      for (; i > -1 && i < start + offset; i =
          mask.nextSetBit(i + 1));
      

      if (c == 0) {
        dupDetect.set(offset);
        c++;
        // The first center point chosen uniformly at random among all data points.
        medoids.add(new Cluster(c, new DataInstance(data.get(i))));
      } else {
        double newMedoidDistSquare = Double.MAX_VALUE;
        for (Cluster prevMedoid : medoids) {
          newMedoidDistSquare = Math.min(newMedoidDistSquare, 
              Math.pow(distFn.distance(data.get(i), 
              prevMedoid.getCentroid(), attributeList), 2));
        }
        
        double sumDistSquare = 0.0;
        for (int j = mask.nextSetBit(0); j > -1; j = mask.nextSetBit(j + 1)) {
          double pointDistSquare = Double.MAX_VALUE;
          for (Cluster prevMedoid : medoids) {
            pointDistSquare = Math.min(pointDistSquare, 
                Math.pow(distFn.distance(data.get(j), 
                prevMedoid.getCentroid(), attributeList), 2));
          }
          
          sumDistSquare += pointDistSquare;
        }
        
        double aceptanceProbability = newMedoidDistSquare / sumDistSquare;
        
        if (rng.nextDouble() <= aceptanceProbability) {
          medoids.add(new Cluster(c, new DataInstance(data.get(i))));
          
          dupDetect.set(offset);
          c++;
        }
      }
    }
    System.out.println("STATUS: Initialized Medoids");
    return medoids;
  }
  
  /**
   * Assigns data points, identified by mask, to the cluster with closest medoid.
   * 
   * @param data all data points
   * @param mask bit vector used identify which data instance need to be clustered.
   * @param clusterMembership contains the cluster ID of all points in data.
   * @param attributeList
   * @param clusterMembershipBitSets an array of bitsets where clusterMembershipBitSets[i].get(j) 
   * is True then data instance j is a member of cluster i.
   * @return
   */
  private boolean assignToCluster(final List<DataInstance> data, final BitSet mask,
      final int[] clusterMembership, final List<Attribute> attributeList,
      BitSet[] clusterMembershipBitSets) {
    boolean clusterMemebershipChange = false;
    for (int i = mask.nextSetBit(0); i > -1; i = mask.nextSetBit(i + 1)) {
      double minD = Double.POSITIVE_INFINITY;
      int prevCluster = clusterMembership[i];
      for (int c = 0; c < clusters.size(); c++) {
        double dist =
            distFn.distance(data.get(i), clusters.get(c).getCentroid(),
                attributeList);
        if (dist < minD) {
          prevCluster = clusterMembership[i];
          clusterMembership[i] = c;
          minD = dist;
          if (prevCluster >= 0) {
            clusterMembershipBitSets[prevCluster].clear(i);
          }
          
          clusterMembershipBitSets[c].set(i);
        }
      }
      if (prevCluster != clusterMembership[i]) {
        clusterMemebershipChange = true;
      }
    }

    return clusterMemebershipChange;
  }


  /**
   * Calculates SSE for each cluster.
   * 
   * @param data all data points
   * @param mask bit vector used identify which data instance need to be clustered.
   * @param clusterMembership contains the cluster ID of all points in data.
   * @param attributeList
   */
  private void calcSSE(List<DataInstance> data, BitSet mask,
      int[] clusterMembership, List<Attribute> attributeList) {
    double[] sse = new double[this.clusters.size()];
    for (int i = mask.nextSetBit(0); i > -1; i = mask.nextSetBit(i + 1)) {
      int cId = clusterMembership[i];
      sse[cId] +=
          Math.pow(distFn.distance(clusters.get(cId).getCentroid(),
              data.get(i), attributeList), 2);
    }
    for (int c = 0; c < clusters.size(); c++) {
      clusters.get(c).setSse(sse[c]);
    }
  }
  
  /**
   * Represents cluster membership as bit vector. 
   * @param clusterMembership
   * @param mask
   */
  private void finalizeClusterMemebership(int[] clusterMembership, BitSet mask) {
    for (Cluster c : clusters) {
      c.getMembers().clear();
    }
    for (int i = mask.nextSetBit(0); i > -1; i = mask.nextSetBit(i + 1)) {
      int cId = clusterMembership[i];
      clusters.get(cId).getMembers().set(i);
    }
  }
  
}
