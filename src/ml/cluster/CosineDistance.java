package ml.cluster;

import java.util.List;
import java.util.Map;

import ml.data.Attribute;
import ml.data.DataInstance;

public class CosineDistance implements DistanceFunction, SimilarityFunction {

  /***
   * Computes the cosine similarity between two data instances based on continues value attributes.
   */
  @Override
  public double similarity(DataInstance d1, DataInstance d2, List<Attribute> attributeList) {
    double dProd = dotProduct(d1, d2, attributeList);
    double mag1 = magnitude(d1, attributeList);
    double mag2 = magnitude(d2, attributeList);
    
    if (mag1 == 0 || mag2 == 0) {
      return 0.0;
    }
    
    double cos = dProd / (mag1 * mag2);
    
    return cos;
  }
  
  /***
   * Computes the cosine distance between two data instances based on continues value attributes.
   */
  @Override
  public double distance(DataInstance d1, DataInstance d2, List<Attribute> attributeList) {
    double cos = similarity(d1, d2, attributeList);
    // Note that, similarity is the inverse of distance(i.e. low dist == high sim)
    // (cos + 1) change cosine range [-1,1] to [0,2]
    //return (cos + 1 == 0? 0 : 1.0 / (cos + 1));
    return 1.0 / (cos + 1.1);
  }
  
  
  
  /***
   * Computes the dot product of two continues values attributes of data instances. 
   * @param d1 data insatnce 1.
   * @param d2 data insatnce 2.
   * @param attributeList list of attributes of the data.
   * @return the dot product.
   */
  public double dotProduct(DataInstance d1, DataInstance d2, List<Attribute> attributeList) {
    double prod = 0.0;

    for (Map.Entry<Integer, Object> me : d1.getAttributes().entrySet()) {
      int index = me.getKey();
      if (attributeList.get(index).getType() != Attribute.Type.CONTINUOUS) {
        continue;
      }
      prod += (Double) d1.getAttributeValueAt(index) * (Double) d2.getAttributeValueAt(index);
    }

    return prod;
  }
  
  /***
   * Coputes the magnitude of continues value attributes of a data instances.
   * @param d1 data insatnce.
   * @param attributeList list of attributes of the data.
   * @return magnitude.
   */
  public double magnitude(DataInstance d1, List<Attribute> attributeList) {
    double mag = 0.0;

    for (Map.Entry<Integer, Object> me : d1.getAttributes().entrySet()) {
      int index = me.getKey();
      if (attributeList.get(index).getType() != Attribute.Type.CONTINUOUS) {
        continue;
      }
      mag += (Double) d1.getAttributeValueAt(index) * (Double) d1.getAttributeValueAt(index);
    }
    
    return Math.sqrt(mag);
  }

}
