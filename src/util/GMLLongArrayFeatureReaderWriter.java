package util;

import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GMLLongArrayFeatureReaderWriter implements GMLFeatureReaderWriter<long[]> {
  private static Pattern nodeFeatureValuePat = Pattern.compile("\\s*val\\s+\"(-?\\d+)\"\\s*");

  @Override
  public String write(long[] feature, String indentation) {
    StringBuilder buff = new StringBuilder();

    buff.append(String.format("%s\tfeatures [\n", indentation));
    for (int i = 0; i < feature.length; i++) {
      buff.append(String.format("%s\t\tval \"", indentation));
      buff.append(feature[i]);
      buff.append("\"\n");
    }
    buff.append(String.format("%s\t]\n", indentation));

    return buff.toString();
  }

  @Override
  public long[] read(Scanner in, Pattern endPat) {
    List<Long> valueList = new LinkedList<>();
    while (in.hasNextLine()) {
      String line = in.nextLine();
      Matcher nodeFeatureValueMat = nodeFeatureValuePat.matcher(line);
      Matcher endMat = endPat.matcher(line);
      if (nodeFeatureValueMat.matches()) {
        valueList.add(Long.parseLong(nodeFeatureValueMat.group(1)));
      } else if (endMat.matches()) {
        long[] valueArray = new long[valueList.size()];
        int i = 0;
        for (long l : valueList) {
          valueArray[i++] = l;
        }
        return valueArray;
      }
    }
    return null;
  }

}
