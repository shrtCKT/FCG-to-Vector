package util.thread;

/***
 * A ThreadSafe Counter
 * @author meha
 *
 */
public class Counter {
  int count;
  public Counter(int initialCount) {
    this.count = initialCount;
  }
  
  public synchronized void increment() {
    count++;
  }
  
  public synchronized void increment(int value) {
    count += value;
  }
  
  public synchronized void decrement() {
    count--;
  }
  
  public synchronized void decrement(int value) {
    count -= value;
  }
  
  public synchronized int getCount() {
    return count;
  }
}