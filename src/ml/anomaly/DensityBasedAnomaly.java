package ml.anomaly;

import java.util.List;
import java.util.PriorityQueue;

import ml.cluster.DistanceFunction;
import ml.data.Attribute;
import ml.data.DataInstance;

public class DensityBasedAnomaly {
  int k;
  DistanceFunction distFn;
  
  /**
   * @param k - size of the nearest neighbor.
   * @param distFn - distance function.
   */
  public DensityBasedAnomaly(int k, DistanceFunction distFn) {
    super();
    this.k = k;
    this.distFn = distFn;
  }

  /**
   * Defines a neighbor data point in terms of its distance and index in data array.
   * @author mehadi
   */
  static class Neighbor implements Comparable<Neighbor>{
    final int index;
    final double dist;
    
    public Neighbor(int index, double dist) {
      super();
      this.index = index;
      this.dist = dist;
    }
    
    public int getIndex() {
      return index;
    }
    
    public double getDist() {
      return dist;
    }

    @Override
    public int compareTo(Neighbor other) {
      return this.dist - other.dist > 0 ? -1 : (this.dist - other.dist < 0 ? 1 : 0) ;
    }

    @Override
    public String toString() {
      return "Neighbor [index=" + index + ", dist=" + dist + "]";
    }
    
  }

  /**
   * Each data point is described in terms of its neighborhood and its index.
   * @author mehadi
   */
  public static class DataPoint {
    final int index;
    PriorityQueue<Neighbor> kNeighbors;
    Double density;
    Double outlierScore;
    int k;
    
    public DataPoint(int index, int k) {
      super();
      this.index = index;
      this.k = k;
      kNeighbors = new PriorityQueue<Neighbor>();
    }
    
    Iterable<Neighbor> getNeighbors() {
      return kNeighbors;
    }
    
    /**
     * Adds a neighbor data point to the neighborhood set.
     * Maintains only the closest K points.
     * @param n
     */
    void addNeghbor(Neighbor n) {
      kNeighbors.add(n);
      if (kNeighbors.size() > k) {
        Neighbor removed = kNeighbors.poll();
      }
    }
    
    public int getIndex() {
      return index;
    }

    /**
     * Returns the density of a data point. 
     * Note that density is calculated on demand. 
     * @return
     */
    public double getDensity() {
      if (density == null) {
        density = calcDensity();
      }
      
      return density;
    }

    public double getNeighborhoodSize() {
      return kNeighbors.size();
    }

    public Double getOutlierScore() {
      return outlierScore;
    }

    void setOutlierScore(Double outlierScore) {
      this.outlierScore = outlierScore;
    }
    
    /**
     * Calculates the density.
     * Density is defined as the inverse of average distance to k nearest neighbors.
     * @return density of a point based on its k neighbors.
     */
    private Double calcDensity() {
      double avergDist = 0.0;
      for (Neighbor n : kNeighbors) {
        avergDist += n.getDist();
      }
      avergDist = avergDist / kNeighbors.size();
          
      return 1.0 / avergDist;
    }

    @Override
    public String toString() {
      return "DataPoint [index=" + index + ", density=" + density
          + ", outlierScore=" + outlierScore + "]";
    }

  }

  /**
   * Detects anomalous data point from the given input data.
   * @param data - input data
   */
  public DataPoint[] detectAnomaly(List<DataInstance> data, List<Attribute> attributeList) {
    DataPoint[] dataPoints = computeNeghborhood(data, attributeList);
    
    calcOutlierScore(dataPoints);
    
    return dataPoints;
  }

  private void calcOutlierScore(DataPoint[] dataPoints) {
    for (DataPoint d : dataPoints) {
      double avergDensity = 0.0;
      for (Neighbor k : d.getNeighbors()) {
        avergDensity += dataPoints[k.getIndex()].getDensity();
      }
      avergDensity = avergDensity / d.getNeighborhoodSize();
      double outlierScore = avergDensity / d.getDensity();
      d.setOutlierScore(outlierScore);
    }
  }

  private DataPoint[] computeNeghborhood(List<DataInstance> data, List<Attribute> attributeList) {
    DataPoint[] dataPoints = new DataPoint[data.size()];
    
    for (int i = 0; i < data.size(); i++) {
      dataPoints[i] = new DataPoint(i, this.k);
    }
    
    for (int i = 0; i < data.size(); i++) {
      for (int j = i + 1; j < data.size(); j++) {
        double dist = distFn.distance(data.get(i), data.get(j), attributeList);
        dataPoints[i].addNeghbor(new Neighbor(j, dist));
        dataPoints[j].addNeghbor(new Neighbor(i, dist));
      }
    }
    
    return dataPoints;
  }
}
