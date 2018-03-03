package util;

/***
 * A class of unordered pair of elements. The order does not mater for hashCode and equals methods.
 * 
 * @author meha
 *
 * @param <T> Data type of the first and second element element.
 */
public class UnorderedPair<T> {
  T first;
  T second;

  public UnorderedPair(T first, T second) {
    super();
    this.first = first;
    this.second = second;
  }

  public T getFirst() {
    return first;
  }

  public void setFirst(T first) {
    this.first = first;
  }

  public T getSecond() {
    return second;
  }

  public void setSecond(T second) {
    this.second = second;
  }

  @Override
  public int hashCode() {
    return ((first == null) ? 0 : first.hashCode()) + ((second == null) ? 0 : second.hashCode());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;

    UnorderedPair<T> other = (UnorderedPair) obj;

    // Case 1 (normal mapping): this.f --> othe.f; this.s --> other.s
    // Case 2 (cross mapping): this.f --> other.s; this.s -- > other.s
    return compareElements(this.first, this.second, other.first, other.second)
        || compareElements(this.first, this.second, other.second, other.first);
  }

  private boolean compareElements(T obj1First, T obj1Second, T obj2First, T obj2Second) {
    if (obj1First == null && obj2First != null) {
      return false;
    } else if (!obj1First.equals(obj2First)) {
      return false;
    }

    if (obj1Second == null && obj2Second != null) {
      return false;
    } else if (!obj1Second.equals(obj2Second)) {
      return false;
    }

    return true;
  }

  @Override
  public String toString() {
    return String.format("(%s,%s)", first == null ? "null" : first.toString(),
        second == null ? "null" : second.toString());
  }
}
