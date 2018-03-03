package ml.cluster;

import java.util.List;

import ml.data.Attribute;
import ml.data.DataInstance;

public interface DistanceFunction {
  /***
   * A function for measuring the distance between two points. Should have the property that the 
   * closer two points are(lower distance) the more similary they should be(high similarity).
   * 
   * @param d1 Data point 1.
   * @param d2 Data point 2.
   * @param attributeList 
   * @return The distance between two data points. The closer the two data points are the more 
   * similar.
   */
  public double distance(DataInstance d1, DataInstance d2, List<Attribute> attributeList);
}
