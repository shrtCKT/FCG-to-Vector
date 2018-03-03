package ml.cluster;

import java.util.BitSet;

import ml.data.DataInstance;

public class Cluster {
  int id;
  /**
   * Indices of of the data point that are member of this cluster.
   */
  BitSet members;

  // For centroid based clustering algorithms
  DataInstance centroid;
  // Metric for centroid based clustering algorithms
  double sse;
  // Metric for meassuring the overall average cossine similarity with in a cluster using 
  // cosine distance function.
  // High value of averageCosineSim means the cluster contains highly similary data points.
  double averageCosineSim;

  public Cluster(int id, DataInstance centroid) {
    this(id, centroid, new BitSet());
  }

  public Cluster(int id, DataInstance centroid, BitSet members) {
    this.id = id;
    this.members = members;
    this.centroid = centroid;
  }

  public int getId() {
    return id;
  }

  public double getSse() {
    return sse;
  }

  public void setSse(double sse) {
    this.sse = sse;
  }

  public DataInstance getCentroid() {
    return centroid;
  }

  public BitSet getMembers() {
    return members;
  }

  public double getCosineAverageSimilarity() {
    return averageCosineSim;
  }
  
  public void setCosineAverageSimilarity(double currCosSim) {
    averageCosineSim = currCosSim;
  }

  public void setCentroid(DataInstance newCentroid) {
    this.centroid = newCentroid;
  }

  public void setClusterID(int id) {
    this.id = id;
  }
}
