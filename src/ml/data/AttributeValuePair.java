package ml.data;

public class AttributeValuePair {
  int attributeIndex;
  Object value;
  public AttributeValuePair(int attributeIndex, Object value) {
    super();
    this.attributeIndex = attributeIndex;
    this.value = value;
  }
  public int getAttributeIndex() {
    return attributeIndex;
  }
  public void setAttributeIndex(int attributeIndex) {
    this.attributeIndex = attributeIndex;
  }
  public Object getValue() {
    return value;
  }
  public void setValue(Object value) {
    this.value = value;
  }
  @Override
  public int hashCode() {
    return new Integer(attributeIndex).hashCode() + 31 * value.hashCode();
  }
  @Override
  public boolean equals(Object obj) {
    if (obj.getClass() != AttributeValuePair.class) {
      return false;
    }
    AttributeValuePair other = (AttributeValuePair) obj;
    if (attributeIndex != other.attributeIndex) {
      return false;
    }
    if (!value.equals(other.value)) {
      return true;
    }
    return true;
  }
}
