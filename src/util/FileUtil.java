package util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class FileUtil {

  /***
   * Returns the list of files, with the given extension, in the given inDir.
   * 
   * @param extension the file with given extension should be listed. If NULL returns all files in
   * directory inDir.
   * @param inDir a File object to the input directory.
   * @return the list of files, with the given extension, in the given inDir.
   */
  public static File[] listFiles(final String extension, File inDir) {
    return listFiles(extension, inDir, false);
  }
  
  public static File[] listFiles(final String extension, File inDir, boolean recursive) {
    Queue<File> dirQ = new LinkedList<File>();
    LinkedList<File> fileList = new LinkedList<File>();
    
    dirQ.add(inDir);
    while (!dirQ.isEmpty()) {
      File head = dirQ.poll();
    
      // List the files in the current dir
      File[] files = head.listFiles(new FilenameFilter() {
        @Override
        public boolean accept(File current, String name) {
          if (extension == null) {
            return true;
          }
          return name.endsWith(extension) && new File(current, name).isFile();
        }
      });
      
      // Add the files to final list
      for (File f : files) {
        fileList.add(f);
      }
      
      // If recursive list the Dirs in the current dir
      if (recursive) {
        File[] dirList = head.listFiles(new FilenameFilter() {
          @Override
          public boolean accept(File current, String name) {
            return new File(current, name).isDirectory();
          }
        });
        
        for (File d : dirList) {
          dirQ.add(d);
        }
      }
    }
    
    File[] allFiles = new File[fileList.size()];
    return fileList.toArray(allFiles);
  }
  
  
  /***
   * Returns a random subset of files, with the given extension, in the inDir .
   * 
   * @param extension the file with given extension should be listed. If NULL returns all files in
   * directory inDir.
   * @param inDir a File object to the input directory.
   * @param selectedProbability each file is included in the return list with the given probability.
   * selectedProbability=1.0 means all and selectedProbability=0 means none.
   * @return a random subset of files, with the given extension, in the inDir.
   */
  public static File[] listFileRandomSubset(final String extension, File inDir,
      double selectedProbability) {
    File[] files = listFiles(extension, inDir);
    if (Double.compare(selectedProbability, 1.0) == 0) {
      return files;
    }
    
    ArrayList<File> newFileList = new ArrayList<File>((int) (files.length * selectedProbability));
    Random rng = new Random(Long.getLong("seed", System.currentTimeMillis()));
    
    for (File f : files) {
      if (rng.nextDouble() < selectedProbability) {
        newFileList.add(f);
      }
    }
    
    File[] newFiles = new File[newFileList.size()];
    return newFileList.toArray(newFiles);
  }

}
