package util;

public class Pair<F, S> {
  F first;
  S second;
  
  public Pair(F first, S second) {
    this.first = first;
    this.second = second;
  }

  public F getFirst() {
    return first;
  }

  public S getSecond() {
    return second;
  }

  public void setFirst(F first) {
    this.first = first;
  }

  public void setSecond(S second) {
    this.second = second;
  }
  
  @Override
  public String toString() {
    return String.format("(%s,%s)", first == null ? "null" : first.toString(),
        second == null ? "null" : second.toString());
  }
}
