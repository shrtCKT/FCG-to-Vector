package util;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import malware.parse.AsmParser.Instruction;

public class GMLInstructuinListFeatureReaderWriter
    implements GMLFeatureReaderWriter<List<Instruction>> {
  private static Pattern nodeFeatureValuePat = 
      Pattern.compile("\\s*val\\s+\"(\\S+(?:\\s*\\S+\\s*)):(\\S+)\"\\s*");
//      Pattern.compile("\\s*val\\s+\"((?:0[Ff]\\s+)?[0-9A-Fa-f]{2}):(\\S+)\"\\s*");
  
  @Override
  public String write(List<Instruction> feature, String indentation) {
    StringBuilder buff = new StringBuilder();

    buff.append(String.format("%s\tfeatures [\n", indentation));
    
    for (Instruction inst : feature) {
      buff.append(String.format("%s\t\tval \"", indentation));
      buff.append(String.format("%s:%s", inst.getOpcode(), inst.getAsm()));
      buff.append("\"\n");
    }
    buff.append(String.format("%s\t]\n", indentation));

    return buff.toString();
  }

  @Override
  public List<Instruction> read(Scanner in, Pattern endPat) {
    List<Instruction> valueList = new ArrayList<Instruction>();
    while (in.hasNextLine()) {
      String line = in.nextLine();
      Matcher nodeFeatureValueMat = nodeFeatureValuePat.matcher(line);
      Matcher endMat = endPat.matcher(line);
      if (nodeFeatureValueMat.matches()) {
        valueList.add(new Instruction(nodeFeatureValueMat.group(2), nodeFeatureValueMat.group(1)));
      } else if (endMat.matches()) {
        return valueList;
      }
    }
    return null;
  }

}
