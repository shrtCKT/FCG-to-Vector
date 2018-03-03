package util;

import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GMLDoubleArrayFeatureReaderWriter implements GMLFeatureReaderWriter<Double[]> {
  private static Pattern nodeFeatureValuePat =
      Pattern.compile("\\s*val\\s+\"(NaN|null|(?:-?\\d+.\\d+(?:E-?\\d+)?))\"\\s*");

  @Override
  public String write(Double[] feature, String indentation) {
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
  public Double[] read(Scanner in, Pattern endPat) {
    List<Double> valueList = new LinkedList<>();
    while (in.hasNextLine()) {
      String line = in.nextLine();
      Matcher nodeFeatureValueMat = nodeFeatureValuePat.matcher(line);
      Matcher endMat = endPat.matcher(line);
      if (nodeFeatureValueMat.matches()) {
        if ("null".equals(nodeFeatureValueMat.group(1))) {
          valueList.add(null);
        } else {
          valueList.add(Double.parseDouble(nodeFeatureValueMat.group(1)));
        }
      } else if (endMat.matches()) {
        Double[] valueArray = new Double[valueList.size()];
        int i = 0;
        for (Double l : valueList) {
          valueArray[i++] = l;
        }
        return valueArray;
      }
    }
    return null;
  }
  
}
