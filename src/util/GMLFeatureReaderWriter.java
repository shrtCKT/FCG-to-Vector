package util;

import java.util.Scanner;
import java.util.regex.Pattern;

public interface GMLFeatureReaderWriter<F> {
  public String write(F feature, String indentation);
  public F read(Scanner in, Pattern endPat); 
}
