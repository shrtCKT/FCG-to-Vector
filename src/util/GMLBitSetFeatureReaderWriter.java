package util;

import java.util.BitSet;
import java.util.Scanner;
import java.util.regex.Pattern;

public class GMLBitSetFeatureReaderWriter implements GMLFeatureReaderWriter<BitSet> {
  GMLLongArrayFeatureReaderWriter readerWriter = new GMLLongArrayFeatureReaderWriter();
  
  @Override
  public String write(BitSet feature, String indentation) {
    long[] longRep = feature.toLongArray();
    return readerWriter.write(longRep, indentation);
  }

  @Override
  public BitSet read(Scanner in, Pattern endPat) {
    long[] longRep = readerWriter.read(in, endPat);
    if (longRep != null) {
      return BitSet.valueOf(longRep);
    }
    return null;
  }

}
