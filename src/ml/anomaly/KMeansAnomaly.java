package ml.anomaly;

import java.util.Arrays;
import java.util.List;

import ml.anomaly.DensityBasedAnomaly.DataPoint;
import ml.cluster.BisectingKMeans;
import ml.cluster.Cluster;
import ml.cluster.DistanceFunction;
import ml.cluster.KMeans;
import ml.data.Attribute;
import ml.data.DataInstance;

public class KMeansAnomaly {
  public enum OutlierScoreType {
    CENTROID_OUTLIER, MEDIAN_OUTLIER
  }

  int k;
  DistanceFunction distFn;
  OutlierScoreType scoreType;

  public KMeansAnomaly(int k, DistanceFunction distFn,
      OutlierScoreType scoreType) {
    super();
    this.k = k;
    this.distFn = distFn;
    this.scoreType = scoreType;
  }

  /**
   * Detects anomalous data point from the given input data.
   * 
   * @param data - input data
   */
  public DataPoint[] detectAnomaly(List<DataInstance> data,
      List<Attribute> attributeList) {
    // perform k means clustering
    KMeans clusteringAlgo = new KMeans(distFn);
    clusteringAlgo.cluster(data, k, attributeList);

    if (scoreType == OutlierScoreType.MEDIAN_OUTLIER) {
      return medianOutlierScore(data, clusteringAlgo.getClusters(),
          attributeList);
    } else if (scoreType == OutlierScoreType.CENTROID_OUTLIER) {
      return centroidOutlierScore(data, clusteringAlgo.getClusters(),
          attributeList);
    }

    return null;
  }

  private DataPoint[] medianOutlierScore(List<DataInstance> data,
      List<Cluster> clusters, List<Attribute> attributeList) {
    DataPoint[] dp = new DataPoint[data.size()];
    for (Cluster c : clusters) {
      if (c.getMembers().cardinality() == 0) {
        continue;
      }
      // find median
      double[] distance = new double[c.getMembers().cardinality()];
      for (int i = c.getMembers().nextSetBit(0), j = 0; i > -1; i =
          c.getMembers().nextSetBit(i + 1), j++) {
        dp[i] = new DataPoint(i, k);
        distance[j] =
            distFn.distance(data.get(i), c.getCentroid(), attributeList);
        dp[i].setOutlierScore(distance[j]);
      }
      Arrays.sort(distance);
      double medianDist =
          distance.length % 2 == 0 ? (distance[(distance.length / 2) - 1] + distance[distance.length / 2]) / 2
              : distance[(distance.length / 2)];

      // calculate relative distance
      for (int i = c.getMembers().nextSetBit(0); i > -1; i =
          c.getMembers().nextSetBit(i + 1)) {
        // for each member set its distance from cluster centroid as its outlier score
        dp[i].setOutlierScore(dp[i].getOutlierScore() / medianDist);
      }
    }

    return dp;
  }

  private DataPoint[] centroidOutlierScore(List<DataInstance> data,
      List<Cluster> clusters, List<Attribute> attributeList) {
    DataPoint[] dp = new DataPoint[data.size()];

    for (Cluster c : clusters) {
      for (int i = c.getMembers().nextSetBit(0); i > -1; i =
          c.getMembers().nextSetBit(i + 1)) {
        // for each member set its distance from cluster centroid as its outlier score
        dp[i] = new DataPoint(i, k);
        dp[i].setOutlierScore(distFn.distance(data.get(i), c.getCentroid(),
            attributeList));
      }
    }

    return dp;
  }
}
