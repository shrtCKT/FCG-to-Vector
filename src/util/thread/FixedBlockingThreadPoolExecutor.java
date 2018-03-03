package util.thread;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class FixedBlockingThreadPoolExecutor extends ThreadPoolExecutor {

  private Semaphore semaphoreNumActiveThreads;
  private int maxNumActiveThreads;

  /***
   * 
   * @param corePoolSize  the number of threads to keep in the pool, even if they are idle, 
   * unless allowCoreThreadTimeOut is set.
   * @param maximumPoolSize the maximum number of threads to allow in the pool
   * @param keepAliveTime when the number of threads is greater than the core, this is the maximum 
   * time that excess idle threads will wait for new tasks before terminating.
   * @param unit the time unit for the keepAliveTime argument
   * @param workQueue the queue to use for holding tasks before they are executed. 
   * This queue, which is a blocking queue, will hold only the Runnable tasks submitted by the 
   * execute method.
   * @param maxNumActiveThreads The maximum nember of actively running threads at a given time.
   */
  public FixedBlockingThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
      TimeUnit unit, BlockingQueue<Runnable> workQueue, int maxNumActiveThreads) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    semaphoreNumActiveThreads = new Semaphore(maxNumActiveThreads);
    this.maxNumActiveThreads = maxNumActiveThreads;
  }
  
  /***
   * Factory Method.
   * 
   * @param maxNumActiveThreads The maximum number of active threads to run at a given time.
   * @param keepAliveTime  when the number of threads is greater than the core, this is the maximum 
   * time that excess idle threads will wait for new tasks before terminating.
   * @param unit the time unit for the keepAliveTime argument
   * @return a FixedBlockingThreadPoolExecutor thread pool executor.
   */
  public static FixedBlockingThreadPoolExecutor threadPoolExecutorFactory(int maxNumActiveThreads,
      long keepAliveTime, TimeUnit unit) {
    BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(maxNumActiveThreads);
    return new FixedBlockingThreadPoolExecutor(maxNumActiveThreads, maxNumActiveThreads, keepAliveTime,
        unit, workQueue, maxNumActiveThreads);
  }
  
  /***
   * Executes a Runnable task. This method blocks, if no thread is available.
   */
  @Override
  public void execute(Runnable task) {
    try {
      semaphoreNumActiveThreads.acquire();
    } catch (InterruptedException e) {
      e.printStackTrace();
      System.exit(1);
    }
    super.execute(task);
  }

  @Override
  public void afterExecute(Runnable r, Throwable t) {
    semaphoreNumActiveThreads.release();
    super.afterExecute(r, t);
  }
  
  /***
   * Wait util all active threads finish.
   */
  public void awaitActiveThreads() {
    try {
      semaphoreNumActiveThreads.acquire(maxNumActiveThreads);
    } catch (InterruptedException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

}
