package ml.random;

public class GraphStateChange implements Change {
  private final int firstIndex;
  private final int secondIndex;
  
  public GraphStateChange(int firstIndex, int secondIndex) {
    this.firstIndex = firstIndex;
    this.secondIndex = secondIndex;
  }

  public int getFirstIndex() {
    return firstIndex;
  }

  public int getSecondIndex() {
    return secondIndex;
  }
  
}
