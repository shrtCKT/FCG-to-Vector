package ml.data;
import java.util.ArrayList;
import java.util.List;

/**
 * Schema of data attribute.
 * @author mehadi
 *
 */
public class Attribute {
  /**
   * Attribute name.
   */
  String name;
  /**
   * The attribute column index in data set.
   */
  int attributeIndex;

  public enum Type {
    DISCRETE, CONTINUOUS, ID
  }
  /**
   * Type of attribute, discrete or continuous.
   */
  Type type;
  /**
   * In case of discrete type attribute the discrete values
   */
  List<String> discreetValues;
  List<String> continuousValuesDefault;


  public Attribute(String name, int attributeIndex, Type type) {
    super();
    this.name = name;
    this.attributeIndex = attributeIndex;
    this.type = type;
    discreetValues = new ArrayList<String>();
    continuousValuesDefault = new ArrayList<String>();
    continuousValuesDefault.add(Boolean.TRUE.toString());
    continuousValuesDefault.add(Boolean.FALSE.toString());
  }

  public Attribute(String name, int attributeIndex, Type type,
      List<String> discreetValues) {
    this(name, attributeIndex, type);
    this.discreetValues = discreetValues;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getAttributeIndex() {
    return attributeIndex;
  }

  public void setAttributeIndex(int attributeIndex) {
    this.attributeIndex = attributeIndex;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public List<String> getValues() {
    if (type == Type.DISCRETE) {
      return discreetValues;
    }
    return continuousValuesDefault;
  }
  
  public void addDiscreteAttributeValue(String value) {
    discreetValues.add(value);
  }

  @Override
  public int hashCode() {
    return name.hashCode() + 31 * attributeIndex;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj.getClass() != Attribute.class) {
      return false;
    }
    Attribute other = (Attribute) obj;
    if (!name.equals(other.name)) {
      return false;
    }
    if (attributeIndex != other.attributeIndex) {
      return false;
    }
    
    return true;
  }
}
