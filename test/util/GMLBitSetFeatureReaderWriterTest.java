package util;

import java.util.BitSet;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.junit.Test;
import static org.junit.Assert.*;

public class GMLBitSetFeatureReaderWriterTest {

  @Test
  public void testReadWrite() {
    BitSet feature = new BitSet();
    feature.set(9928);
    feature.set(3);
    feature.set(5);
    
    GMLBitSetFeatureReaderWriter readerWritter = new GMLBitSetFeatureReaderWriter();
    String buff = readerWritter.write(feature, "\t");
    System.out.println(buff);
    Pattern endPat = Pattern.compile("\\s*\\]\\s*");
    BitSet readFeature = readerWritter.read(new Scanner(buff), endPat);
    
    assertEquals(feature, readFeature);
  }
  
  @Test
  public void testReadWriteEmpty() {
    BitSet feature = new BitSet();
    
    GMLBitSetFeatureReaderWriter readerWritter = new GMLBitSetFeatureReaderWriter();
    String buff = readerWritter.write(feature, "\t");
    System.out.println(buff);
    Pattern endPat = Pattern.compile("\\s*\\]\\s*");
    BitSet readFeature = readerWritter.read(new Scanner(buff), endPat);
    
    assertEquals(feature, readFeature);
  }

}
