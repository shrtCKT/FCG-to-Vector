package ml.data;

import java.util.List;
import java.util.Map;

public class DataPreProcess {
  public static void normalizeData(List<DataInstance> data,
      List<Attribute> attributeList) {
    DataInstance max = new DataInstance();
    DataInstance min = new DataInstance();

    for (Map.Entry<Integer, Object> me : data.get(0).getAttributes().entrySet()) {
      int index = me.getKey();
      if (attributeList.get(index).getType() == Attribute.Type.CONTINUOUS) {
        max.setAttributeValueAt(index, Double.NEGATIVE_INFINITY);
        min.setAttributeValueAt(index, Double.POSITIVE_INFINITY);
      }
    }

    // find min, max
    for (DataInstance d : data) {
      for (Map.Entry<Integer, Object> me : d.getAttributes().entrySet()) {
        int index = me.getKey();
        if (attributeList.get(index).getType() != Attribute.Type.CONTINUOUS) {
          continue;
        }
        if ((Double) d.getAttributeValueAt(index) > (Double) max
            .getAttributeValueAt(index)) {
          max.setAttributeValueAt(index, (Double) d.getAttributeValueAt(index));
        }
        if ((Double) d.getAttributeValueAt(index) < (Double) min
            .getAttributeValueAt(index)) {
          min.setAttributeValueAt(index, (Double) d.getAttributeValueAt(index));
        }

      }
    }

    // normalize
    for (DataInstance d : data) {
      for (Map.Entry<Integer, Object> me : d.getAttributes().entrySet()) {
        int index = me.getKey();
        if (attributeList.get(index).getType() != Attribute.Type.CONTINUOUS) {
          continue;
        }
        double normalizedVal =
            ((Double) d.getAttributeValueAt(index) - (Double) min
                .getAttributeValueAt(index))
                / ((Double) max.getAttributeValueAt(index) - (Double) min
                    .getAttributeValueAt(index));
        d.setAttributeValueAt(index, normalizedVal);
      }
    }
  }
}
