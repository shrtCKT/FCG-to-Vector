package ml.cluster;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ml.data.Attribute;
import ml.data.DataInstance;

public class ClusterEvaluation {

  /***
   * Computes the average Silhouette Coefficient of all data points.
   * 
   * @param data data points.
   * @param clusters the clusters.
   * @param attributeList list of attributes, features.
   * @param distFn distance function used.
   * @return average Silhouette Coefficient of all data points.
   */
  public static double averageSilhouetteCoefficient(List<DataInstance> data,
      List<Cluster> clusters, List<Attribute> attributeList,
      DistanceFunction distFn) {
    double[] sil = silhouetteCoefficient(data, clusters, attributeList, distFn);
    double averageSil = 0.0;
    for (double s : sil) {
      averageSil += s;
    }
    averageSil /= sil.length; 
    return averageSil;
  }
  
  /***
   * Computes the Silhouette Coefficient of each data points.
   * @param data
   * @param clusters
   * @param attributeList
   * @param distFn
   * @return
   */
  public static double[] silhouetteCoefficient(List<DataInstance> data,
      List<Cluster> clusters, List<Attribute> attributeList,
      DistanceFunction distFn) {
    // distance of each point to every other point, for quick lookup.
    double[][] dist = new double[data.size()][data.size()];
    for (int i = 0; i < data.size(); i++) {
      for (int j = 0; j < data.size(); j++) {
        if (i == j) {
          dist[i][j] = 0.0;
          continue;
        }
        dist[i][j] = distFn.distance(data.get(i), data.get(j), attributeList);
      }
    }

    double[] sil = new double[data.size()];

    for (Cluster c : clusters) { // for each cluster, and
      for (int i = c.getMembers().nextSetBit(0); i > -1; i =
          c.getMembers().nextSetBit(i + 1)) { // for each point in that cluster.
        // distance to point with in the same cluster.
        double dWithin = 0.0;
        for (int j = c.getMembers().nextSetBit(0); j > -1; j =
            c.getMembers().nextSetBit(j + 1)) {
          dWithin += dist[i][j];
        }
        // take the average
        dWithin =
            c.members.cardinality() == 0 ? 0.0 : dWithin
                / c.members.cardinality();
        // the minimum average distance to points in other clusters
        double minOtherAverageDist = Double.MAX_VALUE;
        for (Cluster other : clusters) { // for all other clusters
          if (other == c) {
            continue;
          }

          Double averageDist = 0.0;
          for (int j = other.getMembers().nextSetBit(0); j > -1; j =
              other.getMembers().nextSetBit(j + 1)) {
            averageDist += dist[i][j];
          }
          averageDist =
              other.members.cardinality() == 0 ? 0.0 : averageDist
                  / other.members.cardinality();

          minOtherAverageDist = Math.min(minOtherAverageDist, averageDist);
        }

        sil[i] =
            (minOtherAverageDist - dWithin)
                / Math.max(minOtherAverageDist, dWithin);
      }
    }

    return sil;
  }

  /***
   * Computes the over all entropy.
   * @param data
   * @param clusters
   * @param attributeList
   * @param verbose
   * @return
   */
  public static double entropy(List<DataInstance> data, List<Cluster> clusters,
      List<Attribute> attributeList, boolean verbose) {
    double entropy = 0;
    
    Set<String> classes = new HashSet<String>(); 
    for (DataInstance di : data) {
      classes.add(di.getClassLabel());
    }
    
    
    for (Cluster c : clusters) {
      if (c.getMembers().isEmpty()) {
        continue;
      }
      
      double clusterEnt = 0.0;
      HashMap<String, Integer> clusterClassDistribution = new HashMap<String, Integer>();
      for (int i = c.getMembers().nextSetBit(0); i > -1; i = c.getMembers().nextSetBit(i + 1)) {
        Integer count = clusterClassDistribution.get(data.get(i).getClassLabel());
        if (count == null) {
          count = 0;
        }
        clusterClassDistribution.put(data.get(i).getClassLabel(), count + 1);
      }
      
      for (Map.Entry<String, Integer> me : clusterClassDistribution.entrySet()) {
        double prob = (double) me.getValue() / c.getMembers().cardinality();
        clusterEnt += prob * (prob == 0 ? 0 : Math.log(prob));
      }
      clusterEnt = -1 * clusterEnt;
      if (verbose) {
        System.out.printf("Entropy Cluster %2d = %.4f\n", c.getId(), clusterEnt);
      }
      
      entropy +=  c.getMembers().cardinality() * clusterEnt / data.size();
    }
    
    return entropy;
  }
  
  /***
   * Computes the overall F-Measure
   * @param data
   * @param clusters
   * @param attributeList
   * @return
   */
  public static double fMeasure(List<DataInstance> data, List<Cluster> clusters,
      List<Attribute> attributeList) {
    // Overall class distribution
    HashMap<String, Integer> classDistribution = new HashMap<String, Integer>();
    for (DataInstance di : data) {
      Integer count = classDistribution.get(di.getClassLabel());
      if (count == null) {
        count = 0;
      }
      classDistribution.put(di.getClassLabel(), count + 1);
    }
    
    // maps the per-class fmeasure.
    HashMap<String, Double> classMaxF = new HashMap<String, Double>();
    
    for (Cluster c : clusters) {
      if (c.getMembers().isEmpty()) {
        continue;
      }
      
      // Class distribution in current cluster.
      HashMap<String, Integer> clusterClassDistribution = new HashMap<String, Integer>();
      for (int i = c.getMembers().nextSetBit(0); i > -1; i = c.getMembers().nextSetBit(i + 1)) {
        Integer count = clusterClassDistribution.get(data.get(i).getClassLabel());
        if (count == null) {
          count = 0;
        }
        clusterClassDistribution.put(data.get(i).getClassLabel(), count + 1);
      }
      
      // Calc per cluster per class F measure
      for (Map.Entry<String, Integer> me : clusterClassDistribution.entrySet()) {
        double recall  = (double) me.getValue() / c.getMembers().cardinality();
        double precision = (double) me.getValue() / clusterClassDistribution.get(me.getKey());
        double f = (2 * recall * precision) / (recall + precision);
        
        // Note: For an entire hierarchical clustering the F measure of any class is the maximum 
        // value it attains at any node in the tree.
        Double maxF = classMaxF.get(me.getKey());
        classMaxF.put(me.getKey(), maxF == null ? f : Math.max(maxF, f));
      }
    }
    
    // Overall value for the F measure is computed by taking the weighted average of all values for 
    // the F measure
    double fMeasure = 0;
    for (Map.Entry<String, Double> me : classMaxF.entrySet()) {
      double weight = (double) classDistribution.get(me.getKey()) / data.size();
      fMeasure += weight * me.getValue();
    }
    
    return fMeasure;
  }

  /***
   * Computes the overall cosine similarity.
   * @param clusters
   * @param attributeList
   */
  public static void cosineOverallSimilarity(List<Cluster> clusters, List<Attribute> attributeList) {
    CosineDistance cos = new CosineDistance();
    for (Cluster c : clusters) {
      double mag = cos.magnitude(c.getCentroid(), attributeList);
      c.setCosineAverageSimilarity(mag * mag);
    }
  }

  /**
   * Calculates the Rand Statisticts. Applies to data sets with class labels
   * 
   * @param data
   * @param clusters
   * @return
   */
  public static double randStatistic(List<DataInstance> data,
      List<Cluster> clusters) {
    double stat = 0.0;

    int[] memebership = new int[data.size()];
    for (Cluster c : clusters) {
      for (int i = c.getMembers().nextSetBit(0); i > -1; i = c.getMembers()
          .nextSetBit(i + 1)) {
        memebership[i] = c.getId();
      }
    }

    double diferentClassDifferentCluster = 0.0, 
        diferentClassSameCluster = 0.0, 
        sameClassDifferentCluster =0.0, 
        sameClassSameCluster = 0.0;

    for (int i = 0; i < data.size(); i++) {
      for (int j = i + 1; j < data.size(); j++) {
        if (data.get(i).getClassLabel().equals(data.get(j).getClassLabel())
            && memebership[i] == memebership[j]) {
          sameClassSameCluster++;
        } else if (!data.get(i).getClassLabel()
            .equals(data.get(j).getClassLabel())
            && memebership[i] == memebership[j]) {
          diferentClassSameCluster++;
        } else if (data.get(i).getClassLabel()
            .equals(data.get(j).getClassLabel())
            && memebership[i] != memebership[j]) {
          sameClassDifferentCluster++;
        } else {
          diferentClassDifferentCluster++;
        }
      }
    }

    return (diferentClassDifferentCluster + diferentClassSameCluster)
        / (diferentClassDifferentCluster + diferentClassSameCluster
            + sameClassDifferentCluster + sameClassSameCluster);
  }
}
