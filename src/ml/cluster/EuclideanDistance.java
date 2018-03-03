package ml.cluster;

import java.util.List;
import java.util.Map;

import ml.data.Attribute;
import ml.data.DataInstance;

public class EuclideanDistance implements DistanceFunction {

  @Override
  public double distance(DataInstance d1, DataInstance d2,
      List<Attribute> attributeList) {
    double d = 0.0;

    for (Map.Entry<Integer, Object> me : d1.getAttributes().entrySet()) {
      int index = me.getKey();
      if (attributeList.get(index).getType() != Attribute.Type.CONTINUOUS) {
        continue;
      }
      d +=
          Math.pow(
              (Double) d1.getAttributeValueAt(index)
                  - (Double) d2.getAttributeValueAt(index), 2);
    }

    return Math.sqrt(d);
  }

}
