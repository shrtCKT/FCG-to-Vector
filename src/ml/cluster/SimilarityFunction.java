package ml.cluster;

import java.util.List;

import ml.data.Attribute;
import ml.data.DataInstance;

public interface SimilarityFunction {
  /***
   * A function for measuring the similarity between two points. Should have the property that the 
   * more similary(high similarity) two points are, the closer they should be(lower distance).
   * 
   * @param d1 Data point 1.
   * @param d2 Data point 2.
   * @param attributeList 
   * @return The similarity between two data points. The closer the two data points are the more 
   * similar.
   */
  public double similarity(DataInstance d1, DataInstance d2, List<Attribute> attributeList);
}
