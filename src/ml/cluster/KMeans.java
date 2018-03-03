package ml.cluster;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ml.data.Attribute;
import ml.data.DataInstance;

public class KMeans {
  public Random rng;
  DistanceFunction distFn;
  List<Cluster> clusters;

  public KMeans(DistanceFunction distFunction) {
    this.rng = new Random(Long.getLong("seed", System.currentTimeMillis()));
    this.distFn = distFunction;
  }
  
  public KMeans(DistanceFunction distFunction, List<Cluster> clusters) {
    this.rng = new Random(Long.getLong("seed", System.currentTimeMillis()));
    this.distFn = distFunction;
    this.clusters = clusters;
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
    // create a shadow array for data to store cluster membership
    int[] clusterMembership = new int[data.size()];
    for (int i = mask.nextSetBit(0); i > -1; i = mask.nextSetBit(i + 1)) {
      clusterMembership[i] = -1;
    }
    // pick k random data points and set them as centroid
    if (clusters == null) {
      clusters = initialCentroids(data, mask, k);
    }
    
    boolean clusterMemberShipChange = true;
    do {
      // assigned data points to clusters
      clusterMemberShipChange =
          assignToCentroids(data, mask, clusterMembership, attributeList);

      // recompute centroid
      recomputeCentroids(data, mask, clusterMembership, attributeList);
    } while (clusterMemberShipChange);// while stop condition not fulfilled

    if (calcSSE) {
      calcSSE(data, mask, clusterMembership, attributeList);
    }
    finalizeClusterMemebership(clusterMembership, mask);
  }

  /**
   * Creates initial cluster centroids.
   * 
   * @param data all data points.
   * @param mask bit vector used identify which data instance need to be clustered.
   * @param k the number of final clusters.
   * @return List of initial cluster centroids.
   */
  private List<Cluster> initialCentroids(List<DataInstance> data, BitSet mask,
      int k) {
    List<Cluster> centroids = new ArrayList<Cluster>(k);

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
      centroids.add(new Cluster(c, new DataInstance(data.get(i))));
    }

    return centroids;
  }

  /**
   * Assigns data points, identified by mask, to the cluster with closest centroid.
   * 
   * @param data all data points
   * @param mask bit vector used identify which data instance need to be clustered.
   * @param clusterMembership contains the cluster ID of all points in data.
   * @param attributeList
   * @return
   */
  private boolean assignToCentroids(final List<DataInstance> data, final BitSet mask,
      final int[] clusterMembership, final List<Attribute> attributeList) {
    boolean clusterMemebershipChange = false;
    for (int i = mask.nextSetBit(0); i > -1; i = mask.nextSetBit(i + 1)) {
      double minD = Double.POSITIVE_INFINITY;
      int prevCluster = clusterMembership[i];
      for (int c = 0; c < clusters.size(); c++) {
        double dist =
            distFn.distance(data.get(i), clusters.get(c).getCentroid(),
                attributeList);
        if (dist < minD) {
          clusterMembership[i] = c;
          minD = dist;
        }
      }
      if (prevCluster != clusterMembership[i]) {
        clusterMemebershipChange = true;
      }
    }

    return clusterMemebershipChange;
  }

  /**
   * Recomputes the cluster centroids.
   * 
   * @param centroids
   * @param data
   * @param mask
   * @param clusterMembership
   * @param attributeList
   */
  private void recomputeCentroids(final List<DataInstance> data, final BitSet mask,
      final int[] clusterMembership, final List<Attribute> attributeList) {
    // Initialize all of the new cluster centroids to 0
    List<Cluster> newC = new ArrayList<Cluster>();
    int[] count = new int[clusters.size()];
    for (int c = 0; c < clusters.size(); c++) {
      DataInstance di = new DataInstance();
      newC.add(new Cluster(c, di));
      for (Map.Entry<Integer, Object> me : data.get(0).getAttributes()
          .entrySet()) {
        di.setAttributeValueAt(me.getKey(), 0.0);
      }
    }

    // Calculate the distance sum for each centroid to its member data points.
    for (int i = mask.nextSetBit(0); i > -1; i = mask.nextSetBit(i + 1)) {
      count[clusterMembership[i]]++;
      // for each continues attribute in update sum
      for (Map.Entry<Integer, Object> me : data.get(0).getAttributes()
          .entrySet()) {
        int attribIndex = me.getKey();
        // Skip Non-Continuous attributes
        if (attributeList.get(attribIndex).getType() != Attribute.Type.CONTINUOUS) {
          continue;
        }

        double prevAttrSum =
            (Double) newC.get(clusterMembership[i]).getCentroid()
                .getAttributeValueAt(attribIndex);
        newC.get(clusterMembership[i])
            .getCentroid()
            .setAttributeValueAt(
                attribIndex,
                prevAttrSum
                    + (Double) data.get(i).getAttributeValueAt(attribIndex));
      }
    }

    // For each centroid update each attributes average
    for (int c = 0; c < clusters.size(); c++) {
      for (Map.Entry<Integer, Object> me : data.get(0).getAttributes()
          .entrySet()) {
        int attributeIndex = me.getKey();
        clusters
            .get(c)
            .getCentroid()
            .setAttributeValueAt(
                attributeIndex,
                (count[c] == 0 ? 0 : (Double) newC.get(c).getCentroid()
                    .getAttributeValueAt(attributeIndex)
                    / count[c]));
      }
    }
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
