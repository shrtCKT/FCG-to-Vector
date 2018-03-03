package ml.data;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents data instance in the form of attribute-value pair and a class lable.
 * 
 * @author mehadi
 *
 */
public class DataInstance {

  public static class DIComparator implements Comparator<DataInstance> {
    private int attributeIndex;

    public DIComparator(int attributeIndex) {
      this.attributeIndex = attributeIndex;
    }

    @Override
    public int compare(DataInstance di1, DataInstance di2) {
      Double v1 = (Double) di1.getAttributeValueAt(attributeIndex);
      Double v2 = (Double) di2.getAttributeValueAt(attributeIndex);

      return (v1 == v2 ? 0 : (v1 > v2 ? 1 : -1));
    }
  }

  HashMap<Integer, Object> attributes;
  String classLabel;
  
  public DataInstance() {
    attributes = new HashMap<Integer, Object>();
  }
  
  /**
   * Copy Constructor.
   * @param dataInstance
   */
  public DataInstance(DataInstance other) {
    this();
    this.classLabel = other.classLabel;
    for (Map.Entry<Integer, Object> me : other.attributes.entrySet()) {
      attributes.put(me.getKey(), me.getValue());
    }
  }

  public String getClassLabel() {
    return classLabel;
  }

  public void setClassLabel(String classLabel) {
    this.classLabel = classLabel;
  }

  public Object getAttributeValueAt(int attributeIndex) {
    return attributes.get(attributeIndex);
  }
  
  /**
   * Sets the value of an attribute a the specified attributeIndex.
   * Indices need not be contiguous.  
   * @param attributeIndex index
   * @param value attribute value
   */
  public void setAttributeValueAt(int attributeIndex, Object value) {
    attributes.put(attributeIndex, value);
  }
  
  public HashMap<Integer, Object> getAttributes() {
    return attributes;
  }

  public boolean attributeEquals(DataInstance obj, BitSet attributeSet) {
    // We are only using the attributes as a measure of equality this is
    // required for stop condition checking.
    for(int i = attributeSet.nextClearBit(0); i > -1 && i < attributes.size(); i = attributeSet.nextClearBit(i+1)) {
      if(!getAttributeValueAt(i).equals(obj.getAttributeValueAt(i))) {
        return false;
      }
    }
    return true;
  }

  @Override
  public String toString() {
    return "DataInstance [attributes=" + attributes + ", classLabel="
        + classLabel + "]";
  }
}
