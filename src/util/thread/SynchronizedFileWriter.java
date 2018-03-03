package util.thread;

import java.io.IOException;
import java.io.Writer;

/***
 * Thread safe file writer.
 * 
 * @author Mehadi
 *
 */
public class SynchronizedFileWriter {
  Writer out;

  public SynchronizedFileWriter(Writer out) {
    this.out = out;
  }

  public synchronized void write(String buff) throws IOException {
    out.write(buff);
    // System.out.println(buff);
  }

  public synchronized void flush() throws IOException {
    out.flush();
  }

  public synchronized void close() throws IOException {
    out.close();
  }
}